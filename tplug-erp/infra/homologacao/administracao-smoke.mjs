import assert from 'node:assert/strict';
import fs from 'node:fs';
const {chromium}=await import(process.env.PLAYWRIGHT_MODULE||'playwright');
const base=process.argv[2]||'http://127.0.0.1:8082';
const browser=await chromium.launch({...(process.env.PLAYWRIGHT_EXECUTABLE?{executablePath:process.env.PLAYWRIGHT_EXECUTABLE}:{channel:'chrome'}),headless:true,args:['--no-sandbox']});
const page=await browser.newPage({viewport:{width:1680,height:950},serviceWorkers:'block'});
const errors=[];page.on('pageerror',e=>errors.push(e.message));
await page.addInitScript(()=>{localStorage.setItem('tplug_access_token','test-access');localStorage.setItem('tplug_refresh_token','test-refresh');});
const hoje=new Intl.DateTimeFormat('en-CA',{timeZone:'America/Recife'}).format(new Date());
const data={alteracoes:[{tipo:'Upgrade',texto:'Alteração persistida de teste'}],hoje,clientes:[{id:'11111111-1111-4111-8111-111111111111',empresa:'Empresa integração',cnpj:'12345678000190',responsavel:'Responsável',email:'teste@example.test',telefone:'81999999999',plano:'Essencial',segmento:'Varejo',modulos:['ERP'],inicio:hoje,vencimento:'2099-12-31',mensalidade:149.9,situacao:'Ativo',lojas:1,terminais:null,usuarios:1,trial:'2099-12-31',certificado:null,logs:[],faturas:[],notas:[],origemTrial:true,etapa:'Trial ativo',onboarding:'PENDENTE'},{id:'22222222-2222-4222-8222-222222222222',empresa:'Trial integração',cnpj:'22345678000190',responsavel:'Responsável trial',email:'trial@example.test',telefone:'81999999999',plano:'Completo',segmento:'Varejo',modulos:['ERP'],inicio:hoje,vencimento:'2099-12-31',mensalidade:0,situacao:'Trial',lojas:1,terminais:null,usuarios:1,trial:'2099-12-31',certificado:null,logs:[],faturas:[],notas:[],origemTrial:true,etapa:'Trial ativo',onboarding:'PENDENTE'}],faturas:[{id:1,cliente:'11111111-1111-4111-8111-111111111111',numero:'TRX-1',competencia:hoje.slice(0,7),emissao:hoje,vencimento:'2099-12-31',valor:149.9,pago:0,forma:'Pix',data:'',cancelada:false,logs:[]}],catalogo:[{nome:'Pessoal',preco:39.9,usuarios:1,lojas:1},{nome:'Básico',preco:79.9,usuarios:2,lojas:1},{nome:'Essencial',preco:149.9,usuarios:5,lojas:1},{nome:'Completo',preco:249.9,usuarios:10,lojas:3},{nome:'Corporativo',preco:0,usuarios:0,lojas:0}],relatorios:[]};
let negado=false,pagamentos=[],falharPagamento=true;
await page.route('**/api/v1/saas/**',async route=>{
 const req=route.request(),url=new URL(req.url());
 assert.equal(req.headers().authorization,'Bearer test-access');
 if(url.pathname.endsWith('/acesso'))return route.fulfill({json:{permitido:true}});
 if(url.pathname.endsWith('/painel'))return route.fulfill(negado?{status:403,json:{detail:'Restrito'}}:{json:data});
 if(url.pathname.endsWith('/pagamentos')){pagamentos.push(req.postDataJSON());if(falharPagamento){falharPagamento=false;return route.fulfill({status:503,json:{detail:'Tente novamente'}});}data.faturas[0].pago+=req.postDataJSON().valor;return route.fulfill({status:200,body:''});}
 if(url.pathname.endsWith('/prorrogar')){assert.equal(req.postDataJSON().dias,7);return route.fulfill({status:200,body:''});}
 return route.fulfill({status:200,body:''});
});
try{
 await page.goto(base+'/administracao');
 await page.getByRole('heading',{name:'Visão Geral',exact:true}).waitFor();
 fs.mkdirSync('/tmp/traxup-visual/administracao',{recursive:true});
 for(const nome of ['Visão Geral','Clientes','Trials','Financeiro','Planos e Assinaturas','Faturas','Relatórios']){
  await page.getByRole('navigation').getByRole('button',{name:nome,exact:true}).click();
  await page.getByRole('heading',{name:nome,exact:true}).waitFor();
  for(const width of [320,390,768,1680]){await page.setViewportSize({width,height:950});assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),`Transbordamento ${nome} ${width}`);}
  await page.screenshot({path:'/tmp/traxup-visual/administracao/'+nome.replaceAll(' ','-')+'.png',fullPage:true});
 }
 await page.getByRole('navigation').getByRole('button',{name:'Planos e Assinaturas',exact:true}).click();await page.reload();await page.getByRole('heading',{name:'Planos e Assinaturas',exact:true}).waitFor();assert.equal(await page.getByRole('heading',{name:'Upgrades',exact:true}).locator('..').locator('strong').innerText(),'1');await page.getByRole('navigation').getByRole('button',{name:'Relatórios',exact:true}).click();
 await page.emulateMedia({media:'print'});assert(await page.locator('main .distribution').first().isVisible(),'Dados do relatório precisam aparecer na impressão');await page.emulateMedia({media:'screen'});
 await page.getByRole('navigation').getByRole('button',{name:'Trials',exact:true}).click();
 await page.locator('main .company').filter({hasText:'Trial integração'}).click();
 await page.getByRole('button',{name:'Prorrogar trial',exact:true}).click();
 await page.getByRole('button',{name:'Confirmar prorrogação',exact:true}).click();
 await page.getByRole('dialog').waitFor({state:'detached'});
 await page.getByRole('navigation').getByRole('button',{name:'Faturas',exact:true}).click();
 await page.getByRole('button',{name:'Registrar pagamento',exact:true}).click();
 await page.getByLabel('Valor recebido').fill('10');
 await page.getByRole('button',{name:'Confirmar recebimento manual',exact:true}).click();
 await page.getByRole('alert').filter({hasText:'Tente novamente'}).waitFor();
 await page.getByRole('button',{name:'Confirmar recebimento manual',exact:true}).click();
 await page.getByRole('dialog').waitFor({state:'detached'});
 assert.equal(pagamentos.length,2);assert.equal(pagamentos[0].chave,pagamentos[1].chave);assert.equal(pagamentos[1].valor,10);
 negado=true;await page.reload();await page.getByRole('alert').filter({hasText:'Acesso restrito'}).waitFor();
 assert.equal(await page.getByRole('heading',{name:'Clientes',exact:true}).count(),0);
 assert.deepEqual(errors,[]);
 console.log('PASS: sete telas integradas, API autenticada, falha/recuperação, chave de recebimento preservada e responsividade. API simulada apenas neste teste; persistência coberta por SaasIntegrationTest.');
}finally{await browser.close();}
