import assert from 'node:assert/strict';
import {mkdir,readFile} from 'node:fs/promises';
import {pathToFileURL} from 'node:url';
const {chromium}=await import(pathToFileURL(process.env.PLAYWRIGHT_MODULE).href);
const browser=await chromium.launch({...(process.env.PLAYWRIGHT_EXECUTABLE?{executablePath:process.env.PLAYWRIGHT_EXECUTABLE}:{channel:'chrome'}),headless:true,args:['--no-sandbox']});
const out=process.env.VISUAL_OUTPUT||'/tmp/traxup-visual';await mkdir(out,{recursive:true});
try{
 for(const width of [320,390,768,1536]){
  const context=await browser.newContext({viewport:{width,height:1000},serviceWorkers:'block',acceptDownloads:true});
  const page=await context.newPage();const errors=[];page.on('pageerror',e=>errors.push(e.message));
  let listStatus=200;let createStatus=201;let creates=[];let patches=[];
  let produtos=[{id:'p1',codigo:'P001',descricao:'Produto Visual',grupo:'Geral',ncm:'12345678',vendaPrc:20,compraPrc:10,codigoBarra:'789',unidade:'UN',ativo:true,criadoEm:'2026-10-01T12:00:00Z'}, {id:'p2',codigo:'P002',descricao:'Produto Inativo',grupo:'Outro',ncm:'',vendaPrc:0,compraPrc:0,codigoBarra:'',unidade:'UN',ativo:false,criadoEm:'2026-10-01T12:00:00Z'}];
  await page.route('**/api/**',async route=>{
   const req=route.request(),path=new URL(req.url()).pathname;
   if(path==='/api/v1/auth/login')return route.fulfill({json:{accessToken:'produtos-test',refreshToken:'produtos-test',expiresIn:300}});
   if(path==='/api/v1/onboarding')return route.fulfill({json:{concluido:true,empresaConfigurada:true}});
   if(path==='/api/v1/me/filiais')return route.fulfill({json:[{id:'00000000-0000-4000-8000-000000000101',nome:'Matriz',empresaId:'empresa-test'}]});
   if(path==='/api/v1/produtos'&&req.method()==='GET')return route.fulfill({status:listStatus,json:listStatus===200?produtos:{}});
   if(path==='/api/v1/produtos'&&req.method()==='POST'){
    const payload=req.postDataJSON();creates.push(payload);await new Promise(r=>setTimeout(r,100));
    if(createStatus!==201)return route.fulfill({status:createStatus,json:{}});
    const p={...payload,id:'p3',ativo:true,criadoEm:'2026-10-14T00:00:00Z'};produtos=[...produtos,p];return route.fulfill({status:201,json:p});
   }
   if(path==='/api/v1/produtos/p1/desativar'){patches.push(req.method());produtos=produtos.map(p=>p.id==='p1'?{...p,ativo:false}:p);return route.fulfill({json:produtos[0]});}
   return route.fulfill({json:[]});
  });
  await page.goto(`${process.argv[2]}/produtos`);
  await page.getByLabel('Empresa (4 dígitos)',{exact:true}).fill('0042');
  await page.getByLabel('E-mail',{exact:true}).fill('produtos@example.test');
  await page.getByLabel('Senha',{exact:true}).fill('teste-local');
  await page.getByRole('button',{name:'Entrar',exact:true}).click();
  await page.getByRole('heading',{name:'Produtos',exact:true}).waitFor();
  await page.getByRole('button',{name:'Produto Visual',exact:true}).waitFor();
  assert.equal(new URL(page.url()).pathname,'/produtos');
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true,`Overflow produtos ${width}`);
  await page.getByLabel('Buscar produto',{exact:true}).fill('P001');
  assert.equal(await page.getByRole('button',{name:'Produto Inativo',exact:true}).count(),0);
  await page.getByRole('button',{name:'Limpar',exact:true}).click();
  await page.getByLabel('Grupo',{exact:true}).selectOption({label:'Outro'});
  await page.getByLabel('Situação',{exact:true}).selectOption({label:'Ativo'});
  await page.getByText('Nenhum produto encontrado.',{exact:true}).waitFor();
  await page.getByRole('button',{name:'Limpar',exact:true}).click();
  await page.getByRole('button',{name:'Produto Visual',exact:true}).click();
  await page.getByRole('button',{name:'Preços',exact:true}).click();
  await page.getByRole('complementary',{name:'Detalhes do produto'}).getByText('100,0%',{exact:true}).waitFor();
  await page.getByRole('complementary',{name:'Detalhes do produto'}).getByRole('button',{name:'Fiscal',exact:true}).click();
  await page.getByText('12345678',{exact:true}).waitFor();
  await page.screenshot({path:`${out}/produtos-${width}.png`,fullPage:true});
  await page.getByRole('button',{name:'Novo produto',exact:false}).click();
  const dialog=page.getByRole('dialog');await dialog.waitFor();
  await page.getByRole('button',{name:'Salvar produto',exact:true}).click();
  await page.getByRole('alert').filter({hasText:'Preencha código'}).waitFor();assert.equal(creates.length,0);
  await dialog.getByLabel('Código *',{exact:true}).fill('=NOVO');
  await dialog.getByLabel('Descrição *',{exact:true}).fill('Produto Novo');
  await dialog.getByLabel('Preço de custo *',{exact:true}).fill('15');
  await dialog.getByLabel('Preço de venda *',{exact:true}).fill('30');
  createStatus=409;await page.getByRole('button',{name:'Salvar produto',exact:true}).click();
  await page.getByRole('alert').filter({hasText:'Já existe um produto'}).waitFor();
  createStatus=201;await page.getByRole('button',{name:'Salvar produto',exact:true}).click();
  await page.getByRole('status').filter({hasText:'Produto cadastrado com sucesso.'}).waitFor();
  assert.equal(creates.length,2);assert.equal(creates[1].codigo,'=NOVO');assert.equal(creates[1].vendaPrc,30);assert.equal(creates[1].compraPrc,15);
  await page.getByRole('button',{name:'Desativar Produto Visual',exact:true}).click();
  await page.getByRole('dialog').getByRole('button',{name:'Cancelar',exact:true}).click();assert.equal(patches.length,0);
  await page.getByRole('button',{name:'Desativar Produto Visual',exact:true}).click();
  await page.getByRole('button',{name:'Confirmar desativação',exact:true}).click();
  await page.getByRole('status').filter({hasText:'Produto desativado.'}).waitFor();assert.deepEqual(patches,['PATCH']);
  await page.getByLabel('Selecionar Produto Novo',{exact:true}).check();
  const downloadPromise=page.waitForEvent('download');await page.getByRole('button',{name:'Exportar CSV',exact:true}).click();
  const download=await downloadPromise;const csv=await readFile(await download.path(),'utf8');assert.match(csv,/"'=NOVO"/);assert.equal(csv.includes('Produto Visual'),false);
  await page.reload();await page.getByRole('button',{name:'Produto Novo',exact:true}).waitFor();assert.equal(new URL(page.url()).pathname,'/produtos');
  listStatus=403;await page.getByRole('button',{name:'Atualizar produtos',exact:true}).click();
  await page.getByRole('alert').filter({hasText:'Seu perfil não tem permissão'}).waitFor();
  listStatus=200;produtos=[];await page.getByRole('button',{name:'Tentar novamente',exact:true}).click();
  await page.getByText('Nenhum produto encontrado.',{exact:true}).waitFor();
  assert.deepEqual(errors,[]);await context.close();
 }
}finally{await browser.close();}
