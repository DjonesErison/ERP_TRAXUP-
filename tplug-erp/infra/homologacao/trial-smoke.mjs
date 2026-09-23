import assert from 'node:assert/strict';
import { mkdir } from 'node:fs/promises';
import { pathToFileURL } from 'node:url';
const { chromium } = await import(pathToFileURL(process.env.PLAYWRIGHT_MODULE).href);
const browser = await chromium.launch({ ...(process.env.PLAYWRIGHT_EXECUTABLE ? { executablePath: process.env.PLAYWRIGHT_EXECUTABLE } : { channel: 'chrome' }), headless: true, args: ['--no-sandbox'] });
const base = process.argv[2];
const out = process.env.VISUAL_OUTPUT || '/tmp/traxup-visual';
await mkdir(out, { recursive: true });
async function fill(page) {
  for (const [name,value] of Object.entries({nomeCompleto:'  Teste Visual  ',nomeEmpresa:'Empresa de Teste',razaoSocial:'Empresa de Teste Ltda',documento:'11.222.333/0001-81',telefone:'(11) 96123-4567',email:'trial@example.test'})) await page.locator(`[name=${name}]`).fill(value);
  await page.locator('[name=segmento]').selectOption('Varejo');
  await page.locator('[name=aceitouTermos]').check();
}
try {
  for (const width of [320,390,768,1024,1280,1536,2048]) {
    const context = await browser.newContext({ viewport:{width,height:1000}, serviceWorkers:'block' });
    const page = await context.newPage();
    const errors=[]; page.on('pageerror',e=>errors.push(e.message));
    await page.goto(`${base}/teste`);
    await page.getByRole('heading',{name:'Comece seu teste grátis'}).waitFor();
    for (const img of await page.locator('.trial-page img').all()) await img.evaluate(i=>i.decode());
    assert.equal(await page.locator('.top img').evaluate(img=>img.clientWidth/img.clientHeight >= img.naturalWidth/img.naturalHeight),true,`Logo sem recorte horizontal ${width}`);
    const layout = await page.evaluate(()=>{
      const rect=s=>document.querySelector(s).getBoundingClientRect();
      const benefitRects=[...document.querySelectorAll('.checks li')].map(x=>x.getBoundingClientRect());
      const overlap=(a,b)=>a.left<b.right-1&&a.right>b.left+1&&a.top<b.bottom-1&&a.bottom>b.top+1;
      const modules=rect('.modules'),art=rect('.product-art');
      return {overflow:document.documentElement.scrollWidth>innerWidth,benefitsOverlap:benefitRects.some((a,i)=>benefitRects.slice(i+1).some(b=>overlap(a,b))),modulesOverArt:overlap(modules,art),artClipped:art.right>innerWidth||art.left<0,inputs:[...document.querySelectorAll('.grid input,.grid select')].every(x=>x.getBoundingClientRect().width>100)};
    });
    assert.deepEqual(layout,{overflow:false,benefitsOverlap:false,modulesOverArt:false,artClipped:false,inputs:true},`Layout ${width}`);
    assert.equal(await page.locator('.create').isDisabled(),true);
    await page.getByRole('button',{name:'Teste grátis',exact:true}).click();
    assert.equal(await page.locator('[name=nomeCompleto]').evaluate(e=>e===document.activeElement),true);
    await page.locator('h1').click();
    await page.screenshot({path:`${out}/trial-${width}.png`,fullPage:true});
    let calls=[]; let status=400;
    await page.route('**/api/public/trials',async route=>{
      calls.push(route.request().postDataJSON());
      await new Promise(resolve=>setTimeout(resolve,150));
      await route.fulfill({status,json:status===200?{trialId:'trial-test',tenantId:'tenant-test',expiraEm:'2026-09-30',status:'ATIVO',proximoPasso:'ATIVAR_ADMIN',ativacaoToken:'visual-test-token'}:{}});
    });
    await page.locator('[name=aceitouTermos]').check();
    await page.locator('.create').click();
    assert.equal(calls.length,0,'Formulário vazio não envia cadastro');
    await fill(page); await page.locator('.create').click();
    await page.getByRole('alert').filter({hasText:'Confira os dados'}).waitFor();
    status=503; await page.locator('.create').click();
    await page.getByRole('alert').filter({hasText:'Não foi possível iniciar'}).waitFor();
    status=200; await page.locator('.create').click();
    await page.getByRole('heading',{name:'Seu teste começou!'}).waitFor();
    assert.equal(calls.length,3);
    assert.equal(calls[0].nomeCompleto,'Teste Visual'); assert.equal(calls[0].documento,'11222333000181'); assert.equal(calls[0].telefone,'11961234567');
    assert.equal(calls[0].aceitouTermos,true); assert.equal(calls[0].termosVersao,'2026-09');
    assert.ok(calls[0].idempotencyKey); assert.equal(calls[0].idempotencyKey,calls[2].idempotencyKey);
    await page.getByRole('button',{name:'Continuar →',exact:true}).click();
    await page.getByRole('heading',{name:'Crie sua senha'}).waitFor();
    assert.deepEqual(errors,[]);
    await context.close();
  }
  // Email-first signup and deep links, including mobile, expiration and retry.
  for (const width of [320,390,1280]) {
    const context=await browser.newContext({viewport:{width,height:900},serviceWorkers:'block'});
    const page=await context.newPage();
    const tenant='a8d3e764-1e2b-4eb5-8b23-a6fd71192350'; const email='ana+teste@example.test'; const token='a'.repeat(43);
    await page.route('**/api/public/trials',route=>route.fulfill({status:201,json:{trialId:'test',tenantId:tenant,expiraEm:'2026-09-30',status:'ATIVO',proximoPasso:'VERIFICAR_EMAIL',ativacaoToken:null}}));
    await page.goto(`${base}/teste`);await fill(page);await page.locator('.create').click();
    await page.getByText('Vamos enviar o link de ativação', {exact:false}).waitFor();
    assert.equal(await page.getByRole('heading',{name:'Crie sua senha'}).count(),0);
    let resendPayload;
    await page.route('**/api/public/trials/reenviar-ativacao',route=>{resendPayload=route.request().postDataJSON();return route.fulfill({status:202,body:''});});
    await page.getByRole('button',{name:'Reenviar link de ativação'}).click();await page.getByRole('status').waitFor();
    assert.equal(resendPayload.tenantId,tenant);
    await page.screenshot({path:`${out}/trial-email-${width}.png`,fullPage:true});
    let activationPayload;let activationStatus=401;
    await page.route('**/api/v1/auth/ativacao-admin/confirmar',route=>{activationPayload=route.request().postDataJSON();return route.fulfill({status:activationStatus,body:''});});
    await page.goto(`${base}/ativar#token=${token}&empresa=${tenant}&email=${encodeURIComponent(email)}`);
    await page.getByRole('heading',{name:'Crie sua senha'}).waitFor();
    assert.equal(new URL(page.url()).hash,'','Token removed from address/history');
    await page.locator('[name=senha]').fill('SenhaTeste123!');await page.locator('[name=confirmacao]').fill('SenhaTeste123!');
    await page.getByRole('button',{name:'Criar senha e continuar →'}).click();
    await page.getByRole('heading',{name:'Reenviar ativação'}).waitFor();
    assert.deepEqual(activationPayload,{token,novaSenha:'SenhaTeste123!'});
    assert.equal(await page.locator('[name=emailAtivacao]').inputValue(),email);
    assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);
    await page.screenshot({path:`${out}/activation-${width}.png`,fullPage:true});
    activationStatus=204;await page.getByRole('button',{name:'Criar senha e continuar →'}).click();
    await page.getByRole('heading',{name:'Acesse seu ERP'}).waitFor();
    assert.equal(await page.locator('[name=tenantId]').inputValue(),tenant);
    assert.equal(await page.locator('[name=email]').inputValue(),email);
    await page.goto(`${base}/entrar#empresa=${tenant}&email=${encodeURIComponent(email)}`);
    await page.getByRole('heading',{name:'Acesse seu ERP'}).waitFor();
    assert.equal(await page.locator('[name=email]').inputValue(),email);
    await page.goto(`${base}/ativar`);await page.getByRole('heading',{name:'Reenviar ativação'}).waitFor();
    await context.close();
  }
  const context=await browser.newContext(); const page=await context.newPage();
  await page.goto(`${base}/teste`);
  await page.locator('.product-art img').evaluate(i=>i.decode());
  await page.evaluate(()=>navigator.serviceWorker.ready);
  await page.reload();
  await page.locator('.product-art img').evaluate(i=>i.decode());
  const manifest=await page.evaluate(async()=>await (await fetch('/manifest.webmanifest')).json());
  assert.equal(manifest.display,'standalone'); assert.equal(manifest.scope,'/');
  await page.waitForFunction(async()=>!!(await caches.match('/assets/trial-hero-devices.webp')));
  await context.setOffline(true); await page.reload();
  await page.getByRole('heading',{name:'Comece seu teste grátis'}).waitFor();
  await page.locator('.product-art img').evaluate(i=>i.decode());
  await fill(page); await page.locator('.create').click();
  await page.getByRole('alert').filter({hasText:'Não foi possível iniciar'}).waitFor();
  assert.equal(await page.getByRole('heading',{name:'Seu teste começou!'}).count(),0);
  assert.equal(await page.evaluate(async()=>!!(await caches.match('/api/public/trials'))),false);
  await context.close();
  console.log('Trial: sete larguras sem sobreposição; validação, payload, erros, retry, idempotência, ativação e PWA offline verificados.');
} finally {await browser.close();}
