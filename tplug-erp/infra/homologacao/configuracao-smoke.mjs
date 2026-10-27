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
  let empresa = { id: 'empresa-test', responsavel: 'Ana Trial', razaoSocial: 'Trial LTDA', nomeFantasia: 'Trial Loja', cnpj: '12345678000199', email: 'ana@example.test', telefone: '87999999999', segmento: 'Varejo', quantidadeLojas: 2, cep: '', endereco: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '', empresaRevisada: false, equipeRevisada: false };
  let unidades = []; let concluido = false;
  const dados = () => ({ nomeUsuario: 'Ana Trial', empresa, filial: unidades[0] || null, unidades, fiscalConfigurado: false, vendasConfiguradas: false });
  await page.route('**/api/**', async route => {
   const p = new URL(route.request().url()).pathname;
   if (p === '/api/v1/auth/login') return route.fulfill({ json: { accessToken: 'ui030-fixture', refreshToken: 'ui030-fixture' } });
   if (p === '/api/v1/onboarding') return route.fulfill({ json: { concluido, empresaConfigurada: true, filialConfigurada: unidades.length > 0 } });
   if (p === '/api/v1/onboarding/configuracao' && route.request().method() === 'GET') return route.fulfill({ json: dados() });
   if (p === '/api/v1/onboarding/configuracao/empresa') {
    const body = route.request().postDataJSON(); assert.equal(body.email, 'ana@example.test'); assert.equal(body.razaoSocial, 'Trial LTDA');
    empresa = { ...empresa, ...body, empresaRevisada: true };
    unidades = [{ id: 'filial-matriz', codigo: '001', nome: body.nomeFilial, cnpj: empresa.cnpj, matriz: true, cep: null, endereco: null, numero: null, complemento: null, bairro: null, cidade: null, uf: null, estoqueAtivo: true, pdvAtivo: true, fiscalAtivo: false, configuracaoConcluida: false }];
    return route.fulfill({ json: dados() });
   }
   if (p === '/api/v1/onboarding/configuracao/unidades') {
    const body = route.request().postDataJSON(); assert.equal(body.unidades.length, 2); assert.equal(body.unidades.filter(u => u.matriz).length, 1);
    unidades = body.unidades.map((u, i) => ({ ...u, id: u.id || `filial-${i + 1}`, configuracaoConcluida: true }));
    return route.fulfill({ json: dados() });
   }
   if (p === '/api/v1/onboarding/concluir') { concluido = true; return route.fulfill({ json: { concluido: true, empresaConfigurada: true, filialConfigurada: true } }); }
   if (p === '/api/v1/onboarding/configuracao/adiar') return route.fulfill({ status: 200 });
   if (p === '/api/v1/me/filiais') return route.fulfill({ json: unidades.map(u => ({ id: u.id, nome: u.nome, empresaId: 'empresa-test', empresaNome: 'Trial Loja', codigo: u.codigo, matriz: u.matriz, cidade: u.cidade, uf: u.uf })) });
   return route.fulfill({ json: [] });
  });
  await page.goto(`${process.argv[2]}/configuracao`);
  await page.getByRole('heading', { name: 'Acesse seu ERP' }).waitFor();
  assert.equal(await page.locator('app-onboarding').count(), 0);
  await page.getByLabel('Empresa (4 dígitos)', { exact: true }).fill('0042');
  await page.getByLabel('E-mail', { exact: true }).fill('ana@example.test');
  await page.getByLabel('Senha', { exact: true }).fill('SenhaVisual123!');
  await page.getByRole('button', { name: 'Entrar', exact: true }).click();
  await page.getByRole('heading', { name: 'Prepare sua empresa para começar.' }).waitFor();
  await page.getByRole('heading', { name: 'Dados da empresa' }).waitFor();
  assert.equal(new URL(page.url()).pathname, '/onboarding');
  assert.deepEqual(await page.locator('.step small').allTextContents(), ['Empresa', 'Lojas', 'Unidade', 'Revisão', 'Acesso']);
  await page.locator('.brand').evaluate(image => image.decode());
  assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true);
  await page.screenshot({ path: `${out}/configuracao-${viewport.width}.png`, fullPage: true });
  assert.equal(await page.getByLabel('Razão social', { exact: true }).inputValue(), 'Trial LTDA');
  assert.equal(await page.getByLabel('E-mail do responsável', { exact: true }).inputValue(), 'ana@example.test');
  assert.equal(await page.getByRole('button', { name: 'Salvar e continuar' }).isDisabled(), true);
  for (const [label, value] of Object.entries({ 'CEP da matriz': '56000-000', Endereço: 'Rua Comércio', Número: '42', Bairro: 'Centro', Cidade: 'Salgueiro' })) await page.getByLabel(label, { exact: true }).fill(value);
  await page.locator('select[name="uf"]').selectOption('PE');
  await page.getByRole('button', { name: 'Salvar e continuar' }).click();
  await page.getByRole('heading', { name: 'Configuração das lojas' }).waitFor();
  assert.equal(await page.locator('.units article').count(), 2);
  await page.locator('.units article').nth(0).getByRole('button').click();
  await page.getByRole('heading', { name: 'Configure a matriz' }).waitFor();
  await page.getByRole('button', { name: 'Salvar unidade' }).click();
  await page.locator('.units article').nth(1).getByRole('button').click();
  await page.getByRole('heading', { name: 'Configure a filial' }).waitFor();
  for (const [label, value] of Object.entries({ CEP: '50000-000', Endereço: 'Rua Recife', Número: '100', Bairro: 'Boa Vista', Cidade: 'Recife' })) await page.getByLabel(label, { exact: true }).fill(value);
  await page.locator('select[name="uUf"]').selectOption('PE');
  await page.getByRole('button', { name: 'Salvar unidade' }).click();
  await page.getByRole('button', { name: 'Revisar unidades' }).click();
  await page.getByRole('heading', { name: 'Revise suas unidades' }).waitFor();
  assert.equal(await page.locator('.review-row').count(), 2);
  await page.getByRole('button', { name: 'Concluir configuração' }).click();
  await page.getByRole('heading', { name: 'Em qual unidade você quer entrar?' }).waitFor();
  await page.locator('.access-grid button').first().click();
  await page.locator('app-dashboard').waitFor();
  assert.equal(new URL(page.url()).pathname, '/dashboard');
  assert.deepEqual(errors, []);
  await page.close();
 }
 console.log('UI-030 a UI-034: empresa, lojas, unidade, revisão e acesso verificados em desktop e celular.');
} finally { await browser.close(); }
