import assert from 'node:assert/strict';
import { mkdir } from 'node:fs/promises';
import { pathToFileURL } from 'node:url';
const { chromium } = await import(pathToFileURL(process.env.PLAYWRIGHT_MODULE).href);
const browser = await chromium.launch({ channel: 'chrome', headless: true, args: ['--no-sandbox'] });
const out = '/tmp/traxup-visual';
await mkdir(out, { recursive: true });
try {
  for (const viewport of [{ width: 1440, height: 1000 }, { width: 390, height: 844 }]) {
    const page = await browser.newPage({ viewport });
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    page.on('console', message => { if (message.type() === 'error' && !message.text().startsWith('Failed to load resource:')) errors.push(message.text()); });
    await page.goto(process.argv[2]);
    await page.getByRole('heading', { name: 'Acesse seu ERP' }).waitFor();
    await page.locator('img:visible').first().evaluate(image => image.decode());
    assert.equal(await page.locator('img:visible').first().evaluate(image => image.naturalWidth), 1200);
    assert.equal(await page.getByRole('button', { name: 'Entrar', exact: true }).isDisabled(), true);
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true, 'Overflow no login');
    await page.screenshot({ path: `${out}/login-${viewport.width}.png`, fullPage: true });
    await page.getByLabel('Empresa', { exact: true }).fill('00000000-0000-4000-8000-000000000001');
    await page.getByLabel('E-mail', { exact: true }).fill('visual@example.test');
    await page.getByLabel('Senha', { exact: true }).fill('somente-teste-visual');
    await page.getByRole('button', { name: 'Mostrar senha' }).click();
    assert.equal(await page.locator('[name=senha]').getAttribute('type'), 'text');
    await page.getByRole('button', { name: 'Ocultar senha' }).click();
    assert.equal(await page.locator('[name=senha]').getAttribute('type'), 'password');
    // Only this browser uses fixtures; no user is created and no VPS data is changed.
    let accepted = false;
    await page.route('**/api/**', async route => {
      const url = route.request().url();
      if (url.endsWith('/auth/login')) {
        const payload = route.request().postDataJSON();
        assert.equal(payload.tenantId, '00000000-0000-4000-8000-000000000001');
        return route.fulfill({ status: accepted ? 200 : 401, json: accepted ? { accessToken: 'visual-only', refreshToken: 'visual-only', tokenType: 'Bearer', expiresIn: 300 } : {} });
      }
      if (url.endsWith('/auth/logout')) return route.fulfill({ status: 204 });
      const path = new URL(url).pathname;
      const fixtures = {
        '/api/v1/contabilidade/fechamento-mensal': { competencia: '2026-09', xml: { total: 0, arquivados: 0, pendentes: 0, falhas: 0 }, sped: { total: 0, concluidos: 0, pendentes: 0, falhas: 0, cancelados: 0 }, livroCaixa: { lancamentos: 0, entradas: 0, saidas: 0 }, inventario: { concluidos: 0, ajustados: 0, comDivergencias: 0 } },
        '/api/v1/contabilidade/checklist-mensal': { competencia: '2026-09', statusGeral: 'PENDENTE', podeGerarPacote: false, totalPendencias: 0, itens: [] },
        '/api/v1/contabilidade/livro-caixa': { totalLancamentos: 0, totalDisponivel: 0, pagina: 1, totalPaginas: 0, totalEntradas: 0, totalSaidas: 0, saldoPeriodo: 0, lancamentos: [] },
        '/api/v1/contabilidade/inventarios': { totalNaPagina: 0, totalDisponivel: 0, pagina: 1, totalPaginas: 0, inventarios: [] },
        '/api/v1/fiscal/arquivos': { totalRetornado: 0, itens: [] },
        '/api/v1/contabilidade/sped/exportacoes/prontidao': { prontoParaProcessar: false, pendenciaCodigo: 'WORKER_DESABILITADO' }
      };
      if (fixtures[path]) return route.fulfill({ json: fixtures[path] });
      return route.fulfill({ json: url.includes('/recentes') ? { conteudo: [], totalRegistros: 0, totalPaginas: 0, pagina: 0, tamanho: 20 } : [] });
    });
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await page.getByRole('alert').filter({ hasText: 'Empresa, e-mail ou senha inválidos.' }).waitFor();
    accepted = true;
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await page.getByRole('navigation', { name: 'Menu principal' }).waitFor();
    await page.screenshot({ path: `${out}/crm-${viewport.width}.png`, fullPage: true });
    for (const [label, selector] of [['Vendas','app-vendas'],['Compras','app-compras'],['Inventário','app-inventario-mobile'],['Financeiro','app-conciliacao-financeira'],['Contabilidade','app-contabilidade']]) {
      await page.getByRole('navigation').getByRole('button', { name: label, exact: true }).click();
      await page.locator(selector).waitFor();
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true, `Overflow em ${label}`);
      await page.screenshot({ path: `${out}/${selector}-${viewport.width}.png`, fullPage: true });
    }
    await page.getByRole('button', { name: 'Sair', exact: true }).click();
    await page.getByRole('heading', { name: 'Acesse seu ERP' }).waitFor();
    assert.deepEqual(errors, [], 'Erros JavaScript no navegador');
    await page.close();
  }
  console.log('Identidade TRAXUP: logo, login, erro, mostrar senha, navegacao e logout verificados em desktop e celular. Dados autenticados simulados somente no navegador de teste.');
} finally { await browser.close(); }
