import assert from 'node:assert/strict';
import { mkdir } from 'node:fs/promises';
import { pathToFileURL } from 'node:url';
const { chromium } = await import(pathToFileURL(process.env.PLAYWRIGHT_MODULE).href);
const browser = await chromium.launch({ ...(process.env.PLAYWRIGHT_EXECUTABLE ? { executablePath: process.env.PLAYWRIGHT_EXECUTABLE } : { channel: 'chrome' }), headless: true, args: ['--no-sandbox'] });
const out = process.env.VISUAL_OUTPUT || '/tmp/traxup-visual';
await mkdir(out, { recursive: true });
try {
  for (const viewport of [{ width: 1536, height: 1024 }, { width: 390, height: 844 }]) {
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
    let accepted = false;
    let salesMode = 'data';
    let refreshCount = 0;
    await page.route('**/api/**', async route => {
      const url = route.request().url();
      if (url.endsWith('/auth/login')) {
        const payload = route.request().postDataJSON();
        assert.equal(payload.tenantId, '00000000-0000-4000-8000-000000000001');
        return route.fulfill({ status: accepted ? 200 : 401, json: accepted ? { accessToken: 'visual-only', refreshToken: 'visual-only', tokenType: 'Bearer', expiresIn: 300 } : {} });
      }
      if (url.endsWith('/auth/refresh')) {
        refreshCount++;
        return route.fulfill({ status: salesMode === 'expired' ? 401 : 200, json: salesMode === 'expired' ? {} : { accessToken: 'renewed-visual-only', refreshToken: 'renewed-visual-only', tokenType: 'Bearer', expiresIn: 300 } });
      }
      if (url.endsWith('/auth/logout')) return route.fulfill({ status: 204 });
      const path = new URL(url).pathname;
      if (path === '/api/v1/onboarding') return route.fulfill({ json: { concluido: true, empresaConfigurada: true, filialConfigurada: true } });
      if (path === '/api/v1/me/filiais') return route.fulfill({ json: [{ id: '00000000-0000-4000-8000-000000000101', nome: 'Filial Visual', cnpj: '00000000000100', empresaId: '00000000-0000-4000-8000-000000000201', empresaNome: 'Empresa Visual' }] });
      if (path.endsWith('/vendas/pedidos/recentes') && new URL(url).searchParams.get('tamanho') === '5') {
        assert.match(route.request().headers().authorization || '', /^Bearer /);
        if (salesMode === 'denied') return route.fulfill({ status: 403, json: {} });
        if (salesMode === 'error') return route.fulfill({ status: 503, json: {} });
        if (salesMode === 'expired' || (salesMode === 'refresh' && route.request().headers().authorization === 'Bearer visual-only')) return route.fulfill({ status: 401, json: {} });
        return route.fulfill({ json: { conteudo: salesMode === 'empty' ? [] : [{ id: 'fixture-venda', numero: 'TESTE-001', filialId: 'fixture-filial', status: 'FATURADO', totalLiquido: 289.9, criadoEm: '2026-09-16T12:00:00Z', atualizadoEm: '2026-09-16T12:00:00Z' }], totalRegistros: salesMode === 'empty' ? 0 : 21, totalPaginas: salesMode === 'empty' ? 0 : 5, pagina: 0, tamanho: 5 } });
      }
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
    await page.getByRole('button', { name: 'Esqueci minha senha' }).click();
    await page.getByRole('dialog').waitFor();
    await page.keyboard.press('Escape');
    await page.getByRole('checkbox', { name: 'Lembrar empresa e e-mail' }).check();
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await page.getByRole('alert').filter({ hasText: 'Empresa, e-mail ou senha inválidos.' }).waitFor();
    accepted = true;
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await page.getByRole('navigation', { name: 'Menu principal' }).waitFor();
    await page.getByRole('heading', { name: 'Olá, boas-vindas!' }).waitFor();
    await page.getByText('TESTE-001', { exact: true }).waitFor();
    assert.equal(await page.locator('.summary-card').nth(1).locator('strong').innerText(), '21');
    assert.equal(await page.locator('.summary-card').nth(0).locator('strong').innerText(), '—');
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true, 'Overflow no dashboard');
    assert.equal(await page.getByRole('button', { name: 'Baixar PDV indisponível' }).isDisabled(), true);
    await page.screenshot({ path: `${out}/dashboard-${viewport.width}.png`, fullPage: true });
    for (const [mode, expected] of [['empty', 'Nenhuma venda registrada.'], ['denied', 'Seu perfil não tem permissão para consultar as vendas.'], ['error', 'Não foi possível carregar as vendas. Tente novamente.']]) {
      salesMode = mode;
      await page.getByRole('navigation').getByRole('button', { name: 'Vendas', exact: true }).click();
      await page.getByRole('navigation').getByRole('button', { name: 'Visão Geral', exact: true }).click();
      await page.getByText(expected, { exact: mode === 'empty' }).waitFor();
      assert.equal(await page.locator('.recent-sales').getByText('TESTE-001').count(), 0);
    }
    salesMode = 'data';
    await page.getByRole('button', { name: 'Tentar novamente' }).click();
    await page.getByText('TESTE-001', { exact: true }).waitFor();
    await page.getByRole('button', { name: 'Alternar menu' }).click();
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true, 'Overflow com menu recolhido');
    await page.getByRole('button', { name: 'Alternar menu' }).click();
    await page.getByRole('navigation').getByRole('button', { name: 'CRM', exact: true }).click();
    await page.getByRole('heading', { name: 'Painel de relacionamento' }).waitFor();
    await page.screenshot({ path: `${out}/crm-${viewport.width}.png`, fullPage: true });
    for (const [label, selector] of [['Vendas','app-vendas'],['Compras','app-compras'],['Inventário','app-inventario-mobile'],['Financeiro','app-conciliacao-financeira'],['Contabilidade','app-contabilidade']]) {
      await page.getByRole('navigation').getByRole('button', { name: label, exact: true }).click();
      await page.locator(selector).waitFor();
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true, `Overflow em ${label}`);
      await page.screenshot({ path: `${out}/${selector}-${viewport.width}.png`, fullPage: true });
    }
    await page.getByRole('button', { name: 'Sair', exact: true }).click();
    await page.getByRole('heading', { name: 'Acesse seu ERP' }).waitFor();
    assert.equal(await page.evaluate(() => localStorage.getItem('tplug_access_token')), null);
    assert.equal(await page.evaluate(() => localStorage.getItem('traxup_filial_ativa')), null);
    const hint = await page.evaluate(() => JSON.parse(localStorage.getItem('traxup_login_hint')));
    assert.deepEqual(hint, { tenantId: '00000000-0000-4000-8000-000000000001', email: 'visual@example.test' });
    await page.reload();
    await page.getByRole('heading', { name: 'Acesse seu ERP' }).waitFor();
    assert.equal(await page.getByLabel('E-mail', { exact: true }).inputValue(), 'visual@example.test');
    assert.equal(await page.getByLabel('Senha', { exact: true }).inputValue(), '');
    await page.getByRole('checkbox', { name: 'Lembrar empresa e e-mail' }).uncheck();
    await page.getByLabel('Senha', { exact: true }).fill('somente-teste-visual');
    salesMode = 'refresh';
    await page.getByRole('button', { name: 'Entrar', exact: true }).click();
    await page.getByText('TESTE-001', { exact: true }).waitFor();
    assert.equal(refreshCount, 1, 'A renovação da sessão deve ocorrer uma vez');
    assert.equal(await page.evaluate(() => localStorage.getItem('traxup_login_hint')), null);
    salesMode = 'expired';
    await page.getByRole('navigation').getByRole('button', { name: 'Vendas', exact: true }).click();
    await page.getByRole('navigation').getByRole('button', { name: 'Visão Geral', exact: true }).click();
    await page.getByRole('alert').filter({ hasText: 'Sua sessão expirou. Entre novamente.' }).waitFor();
    assert.equal(await page.evaluate(() => localStorage.getItem('tplug_access_token')), null);
    assert.deepEqual(errors, [], 'Erros JavaScript no navegador');
    await page.close();
  }
  console.log('TRAXUP Etapa 1: login, contexto de filial, home, dados/vazio/erro/403, refresh, expiracao, navegacao e logout verificados em desktop e celular.');
} finally { await browser.close(); }
