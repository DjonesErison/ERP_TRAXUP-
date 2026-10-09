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
  const errors = []; page.on('pageerror', e => errors.push(e.message));
  let empresa = { id: 'empresa-test', responsavel: 'Ana Trial', razaoSocial: 'Trial LTDA', nomeFantasia: 'Trial Loja', cnpj: '12345678000199', email: 'ana@example.test', telefone: '87999999999', segmento: 'Varejo', quantidadeLojas: 2, empresaRevisada: false, equipeRevisada: false };
  let filial = null; let fiscalConfigurado = false; let skipped = false;
  const dados = () => ({ nomeUsuario: 'Ana Trial', empresa, filial, fiscalConfigurado, vendasConfiguradas: false });
  await page.route('**/api/**', async route => {
   const p = new URL(route.request().url()).pathname;
   if (p === '/api/v1/auth/login') return route.fulfill({ json: { accessToken: 'ui019-fixture', refreshToken: 'ui019-fixture' } });
   if (p === '/api/v1/onboarding') return route.fulfill({ json: { concluido: false, empresaConfigurada: true, filialConfigurada: !!filial } });
   if (p === '/api/v1/onboarding/configuracao') return route.fulfill({ json: dados() });
   if (p === '/api/v1/onboarding/configuracao/empresa') {
    const body = route.request().postDataJSON();
    assert.equal(body.email, 'ana@example.test'); assert.equal(body.razaoSocial, 'Trial LTDA');
    empresa = { ...empresa, ...body, empresaRevisada: true }; filial = { id: 'filial-test', nome: body.nomeFilial, cnpj: empresa.cnpj };
    return route.fulfill({ json: dados() });
   }
   if (p === '/api/v1/fiscal/perfis-filial/filial-test') { fiscalConfigurado = true; return route.fulfill({ json: { regimeTributario: 'SIMPLES_NACIONAL', crt: 1, ambiente: 'HOMOLOGACAO', serieNfe: 1, serieNfce: 1 } }); }
   if (p === '/api/v1/usuarios') return route.fulfill({ json: [{ id: 'usuario-test', nome: 'Ana Trial', email: 'ana@example.test', ativo: true }] });
   if (p === '/api/v1/rbac/perfis') return route.fulfill({ json: [{ id: 'perfil-test', nome: 'ADMIN', descricao: 'Administrador' }] });
   if (p === '/api/v1/onboarding/configuracao/adiar') { skipped = true; return route.fulfill({ status: 200 }); }
   if (p === '/api/v1/me/filiais') return route.fulfill({ json: [{ id: 'filial-test', nome: 'Matriz', empresaId: 'empresa-test' }] });
   return route.fulfill({ json: [] });
  });
  // A direct configuration URL requires login before any customer data is shown.
  await page.goto(`${process.argv[2]}/configuracao`);
  await page.getByRole('heading', { name: 'Acesse seu ERP' }).waitFor();
  assert.equal(await page.locator('app-onboarding').count(), 0);
  await page.getByLabel('Empresa (4 dígitos)', { exact: true }).fill('0042');
  await page.getByLabel('E-mail', { exact: true }).fill('ana@example.test');
  await page.getByLabel('Senha', { exact: true }).fill('SenhaVisual123!');
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await page.getByRole('heading', { name: 'Bem-vindo ao TraxUp ERP!' }).waitFor();
  assert.equal(new URL(page.url()).pathname, '/configuracao');
  await page.getByText('Olá, Ana Trial').waitFor();
  assert.deepEqual(await page.locator('.step-copy strong').allTextContents(), ['1. Dados da empresa', '2. Configuração fiscal', '3. Usuários e permissões', '4. Vendas e PDV']);
  assert.equal(await page.locator('[role=progressbar]').getAttribute('aria-valuenow'), '0');
  assert.equal(await page.locator('.done').count(), 0, 'Sem marca falsa de etapa concluída');
  await page.locator('.config-header img').evaluate(image => image.decode());
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true);
  await page.screenshot({ path: `${out}/configuracao-${viewport.width}.png`, fullPage: true });
  await page.getByRole('button', { name: 'Iniciar configuração' }).click();
  assert.equal(await page.getByLabel('Razão social', { exact: true }).inputValue(), 'Trial LTDA');
  assert.equal(await page.getByLabel('Nome fantasia', { exact: true }).inputValue(), 'Trial Loja');
  assert.equal(await page.getByLabel('E-mail', { exact: true }).inputValue(), 'ana@example.test');
  assert.equal(await page.getByRole('button', { name: 'Salvar e continuar' }).isDisabled(), true);
  for (const [label, value] of Object.entries({ CEP: '56000-000', Endereço: 'Rua Comércio', Número: '42', Bairro: 'Centro', Cidade: 'Salgueiro' })) await page.getByLabel(label, { exact: true }).fill(value);
  await page.getByLabel('UF', { exact: true }).selectOption('PE');
  await page.getByRole('button', { name: 'Salvar e continuar' }).click();
  await page.getByRole('heading', { name: '2. Configuração fiscal', exact: true }).waitFor();
  assert.equal(await page.locator('[role=progressbar]').getAttribute('aria-valuenow'), '25');
  await page.getByRole('button', { name: 'Salvar e continuar' }).click();
  await page.getByRole('heading', { name: '3. Usuários e permissões', exact: true }).waitFor();
  await page.getByText('ana@example.test', { exact: true }).waitFor();
  await page.getByRole('button', { name: 'Voltar às etapas' }).click();
  await page.getByRole('button', { name: /4\. Vendas e PDV/ }).click();
  await page.getByRole('heading', { name: '4. Vendas e PDV', exact: true }).waitFor();
  for (const titulo of ['3. Usuários e permissões', '2. Configuração fiscal', '1. Dados da empresa']) {
   await page.getByRole('button', { name: '← Voltar', exact: true }).click();
   await page.getByRole('heading', { name: titulo, exact: true }).waitFor();
  }
  assert.equal(await page.getByLabel('Endereço', { exact: true }).inputValue(), 'Rua Comércio');
  await page.getByRole('button', { name: '← Voltar', exact: true }).click();
  await page.getByRole('button', { name: 'Iniciar configuração' }).waitFor();
  await page.getByRole('button', { name: 'Continuar depois' }).click();
  assert.equal(skipped, true);
  await page.locator('app-onboarding').waitFor({ state: 'detached' });
  assert.deepEqual(errors, []);
  await page.close();
 }
 console.log('UI-019: login protegido, ordem, trial preenchido, persistência, progresso e adiar verificados.');
} finally { await browser.close(); }
