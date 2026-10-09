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
}
try {
  for (const width of [320,390,768,1024,1280,1536,2048]) {
    const context = await browser.newContext({ viewport:{width,height:1000}, serviceWorkers:'block' });
    const page = await context.newPage();
    const errors=[]; page.on('pageerror',e=>errors.push(e.message));
    await page.goto(`${base}/teste`);
    await page.getByRole('heading',{name:'Comece seu teste gratuito'}).waitFor();
    for (const img of await page.locator('.trial-v2 img').all()) await img.evaluate(i=>i.decode());
    const layout = await page.evaluate(()=>{
      const fields=[...document.querySelectorAll('.fields input,.fields select')];
      const card=document.querySelector('.trial-card')?.getBoundingClientRect();
      return {
        overflow:document.documentElement.scrollWidth>innerWidth,
        fieldCount:fields.length,
        inputs:fields.every(x=>x.getBoundingClientRect().width>100 && x.getBoundingClientRect().height>=44),
        cardVisible:!!card && card.width>280 && card.left>=0 && card.right<=innerWidth+1
      };
    });
    assert.deepEqual(layout,{overflow:false,fieldCount:8,inputs:true,cardVisible:true},`Trial 2.0 layout ${width}`);
    assert.equal(await page.locator('.existing').count(),0,'Trial 2.0 não exibe login no cadastro');
    await page.getByRole('link',{name:'Políticas de Privacidade'}).waitFor();
    await page.getByRole('link',{name:'Termos de uso'}).waitFor();
    assert.equal(await page.locator('.create').isDisabled(),false);
    await page.locator('[name=nomeCompleto]').focus();
    assert.equal(await page.locator('[name=nomeCompleto]').evaluate(e=>e===document.activeElement),true);
    await page.getByRole('heading',{name:'Comece seu teste gratuito'}).click();
    await page.screenshot({path:`${out}/trial-${width}.png`,fullPage:true});
    let calls=[]; let status=400;
    await page.route('**/api/public/trials',async route=>{
      calls.push(route.request().postDataJSON());
      await new Promise(resolve=>setTimeout(resolve,150));
      await route.fulfill({status,json:status===200?{trialId:'trial-test',tenantId:'tenant-test',codigoEmpresa:'0042',expiraEm:'2026-09-30',status:'ATIVO',proximoPasso:'ATIVAR_ADMIN',ativacaoToken:'visual-test-token'}:{}});
    });
      await page.locator('.create').click();
    assert.equal(calls.length,0,'Formulário vazio não envia cadastro');
    await fill(page); await page.locator('.create').click();
    await page.getByRole('alert').filter({hasText:'Dados recusados pelo servidor'}).waitFor();
    status=503; await page.locator('.create').click();
    await page.getByRole('alert').filter({hasText:'Não foi possível iniciar'}).waitFor();
    status=409; await page.locator('.create').click();
    await page.getByRole('alert').filter({hasText:'Já existe um cadastro com esses dados'}).waitFor();
    let recovery;
    await page.route('**/api/public/trials/recuperar-acesso',route=>{recovery=route.request().postDataJSON();return route.fulfill({status:202,body:''});});
    await page.getByRole('button',{name:'Recuperar acesso',exact:true}).click();
    await page.getByRole('status').filter({hasText:'Se os dados corresponderem'}).waitFor();
    assert.equal(recovery.email,'trial@example.test');
    assert.equal(recovery.documento,'11.222.333/0001-81');
    status=200; await page.locator('.create').click();
    await page.getByRole('heading',{name:'Seu teste começou!'}).waitFor();
    assert.equal(calls.length,4);
    await page.getByText('0042', {exact:true}).waitFor();
    assert.equal(await page.getByText('tenant-test', {exact:true}).count(),0);
    assert.equal(calls[0].nomeCompleto,'Teste Visual'); assert.equal(calls[0].documento,'11222333000181'); assert.equal(calls[0].telefone,'11961234567');
    assert.equal(calls[0].aceitouTermos,true); assert.equal(calls[0].termosVersao,'2026-09');
    assert.ok(calls[0].idempotencyKey); assert.equal(calls[0].idempotencyKey,calls[2].idempotencyKey);
    await page.getByRole('button',{name:'Continuar',exact:true}).click();
    await page.getByRole('heading',{name:'Ative sua conta'}).waitFor();
    assert.deepEqual(errors,[]);
    await context.close();
  }
  // An existing account must never hide activation or override an email's company.
  {
    const context=await browser.newContext({serviceWorkers:'block'});
    const page=await context.newPage();
    const tenant='0042';
    let oldSessionRequests=0;
    await page.route('**/api/v1/onboarding',route=>{oldSessionRequests++;return route.fulfill({json:{concluido:false,empresaConfigurada:false,filialConfigurada:false}});});
    await page.addInitScript(()=>{
      localStorage.setItem('tplug_access_token','old-account-token');
      localStorage.setItem('tplug_refresh_token','old-refresh-token');
      localStorage.setItem('tplug_tenant_id','11111111-1111-4111-8111-111111111111');
    });
    await page.goto(`${base}/ativar#token=${'a'.repeat(43)}&empresa=${tenant}&email=ana%40example.test`);
    await page.getByRole('heading',{name:'Ative sua conta'}).waitFor();
    assert.equal(oldSessionRequests,0,'Activation must not query the previous account');
    assert.equal(await page.evaluate(()=>localStorage.getItem('tplug_access_token')),null);
    await page.goto('about:blank');
    await page.goto(`${base}/entrar#empresa=${tenant}&email=ana%40example.test`);
    await page.getByRole('heading',{name:'Acesse seu ERP'}).waitFor();
    assert.equal(await page.locator('#login-company').inputValue(),tenant);
    assert.equal(await page.locator('#login-email').inputValue(),'ana@example.test');
    assert.equal(oldSessionRequests,0,'Email login must not query the previous account');
    // A stale bare login with no company must recover instead of trapping the user in onboarding.
    await page.goto('about:blank');
    await page.goto(`${base}/entrar`);
    await page.getByRole('alert').filter({hasText:'Esta sessão não possui uma empresa cadastrada'}).waitFor();
    assert.equal(await page.locator('app-onboarding').count(),0);
    await context.close();
  }
  // Email-first signup and deep links, including mobile, expiration and retry.
  for (const width of [320,390,1280,1536]) {
    const context=await browser.newContext({viewport:{width,height:900},serviceWorkers:'block'});
    const page=await context.newPage();
    const tenant='a8d3e764-1e2b-4eb5-8b23-a6fd71192350'; const codigo='0042'; const email='ana+teste@example.test'; const token='a'.repeat(43);
    await page.route('**/api/public/trials',route=>route.fulfill({status:201,json:{trialId:'test',tenantId:tenant,codigoEmpresa:codigo,expiraEm:'2026-09-30',status:'ATIVO',proximoPasso:'VERIFICAR_EMAIL',ativacaoToken:null}}));
    await page.goto(`${base}/teste`);await fill(page);await page.locator('.create').click();
    await page.getByText('Enviaremos o link de ativação', {exact:false}).waitFor();
    assert.equal(await page.getByRole('heading',{name:'Ative sua conta'}).count(),0);
    let resendPayload;
    await page.route('**/api/public/trials/reenviar-ativacao',route=>{resendPayload=route.request().postDataJSON();return route.fulfill({status:202,body:''});});
    await page.getByRole('button',{name:'Reenviar link de ativação'}).click();await page.getByRole('status').waitFor();
    assert.equal(resendPayload.codigoEmpresa,codigo);
    await page.screenshot({path:`${out}/trial-email-${width}.png`,fullPage:true});
    let activationPayload;let activationStatus=401;let activationRequests=0;
    await page.route('**/api/v1/auth/ativacao-admin/confirmar',route=>{activationRequests++;activationPayload=route.request().postDataJSON();return route.fulfill({status:activationStatus,body:''});});
    await page.goto(`${base}/ativar#token=${token}&empresa=${codigo}&email=${encodeURIComponent(email)}`);
    await page.getByRole('heading',{name:'Ative sua conta'}).waitFor();
    assert.equal(new URL(page.url()).hash,'','Token removed from address/history');
    const officialArt=page.locator('.activation .official-art');
    const officialLogo=page.locator('.activation .logo');
    await officialArt.evaluate(image=>image.decode());
    await officialLogo.evaluate(image=>image.decode());
    assert.match(await officialArt.getAttribute('src'),/\/13-identidade-visual-2\.0\/Ativa%C3%A7%C3%A3o%20de%20Conta%20TRAXUP\.png$/);
    assert.match(await officialLogo.getAttribute('src'),/^assets\/Logo\.png$/);
    assert.deepEqual(await officialLogo.evaluate(image=>[image.naturalWidth,image.naturalHeight]),[1536,1024]);
    assert.equal(await page.locator('.steps [aria-current=step]').innerText(),'2\nAtivação');
    assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false,'Ativação oficial sem overflow');
    await page.screenshot({path:`${out}/activation-clean-${width}.png`,fullPage:true});
    await page.getByRole('button',{name:'Ativar minha conta →'}).click();
    assert.equal(activationRequests,0,'Senha vazia não chama API');
    await page.locator('[name=senha]').fill('curta1');await page.locator('[name=confirmacao]').fill('curta1');
    await page.getByRole('button',{name:'Ativar minha conta →'}).click();
    assert.equal(activationRequests,0,'Senha curta não chama API');
    await page.locator('[name=senha]').fill('SenhaTeste123');
    assert.equal(await page.locator('.requirements .ok').count(),2,'Símbolos continuam opcionais');
    await page.locator('[name=confirmacao]').fill('SenhaDiferente123');
    await page.getByRole('button',{name:'Ativar minha conta →'}).click();
    await page.getByText('As senhas não coincidem.',{exact:true}).waitFor();
    assert.equal(activationRequests,0,'Senhas diferentes não chamam API');
    await page.getByRole('button',{name:'Mostrar senha',exact:true}).click();
    assert.equal(await page.locator('[name=senha]').getAttribute('type'),'text');
    await page.getByRole('button',{name:'Ocultar senha',exact:true}).click();
    assert.equal(await page.locator('[name=senha]').getAttribute('type'),'password');
    await page.getByRole('button',{name:'Mostrar confirmação',exact:true}).click();
    assert.equal(await page.locator('[name=confirmacao]').getAttribute('type'),'text');
    await page.getByRole('button',{name:'Ocultar confirmação',exact:true}).click();
    assert.equal(await page.locator('[name=confirmacao]').getAttribute('type'),'password');
    await page.locator('[name=senha]').fill('SenhaTeste123!');await page.locator('[name=confirmacao]').fill('SenhaTeste123!');
    await page.getByRole('button',{name:'Ativar minha conta →'}).click();
    await page.getByRole('heading',{name:'Reenviar ativação'}).waitFor();
    assert.deepEqual(activationPayload,{token,novaSenha:'SenhaTeste123!'});
    assert.equal(await page.locator('[name=emailAtivacao]').inputValue(),email);
    assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth),false);
    await page.screenshot({path:`${out}/activation-${width}.png`,fullPage:true});
    activationStatus=204;await page.getByRole('button',{name:'Ativar minha conta →'}).click();
    await page.getByRole('heading',{name:'Acesse seu ERP'}).waitFor();
    assert.equal(await page.locator('[name=codigoEmpresa]').inputValue(),codigo);
    assert.equal(await page.locator('[name=email]').inputValue(),email);
    await page.goto(`${base}/entrar#empresa=${codigo}&email=${encodeURIComponent(email)}`);
    await page.getByRole('heading',{name:'Acesse seu ERP'}).waitFor();
    assert.equal(await page.locator('[name=email]').inputValue(),email);
    await page.goto(`${base}/ativar`);await page.getByRole('heading',{name:'Reenviar ativação'}).waitFor();
    await context.close();
  }
  // Recovery links clear prior sessions, keep secrets out of history and use the recovery API.
  {
    const context=await browser.newContext({serviceWorkers:'block'}); const page=await context.newPage();
    let payload; let code=401;
    await page.route('**/api/v1/auth/recuperacao-senha/confirmar',route=>{payload=route.request().postDataJSON();return route.fulfill({status:code,body:''});});
    await page.addInitScript(()=>{localStorage.setItem('tplug_access_token','old-token');localStorage.setItem('tplug_refresh_token','old-refresh');});
    await page.goto(`${base}/recuperar#token=${'r'.repeat(43)}&empresa=0042&email=ana%40example.test`);
    await page.getByRole('heading',{name:'Defina uma nova senha'}).waitFor();
    assert.equal(new URL(page.url()).hash,'');
    assert.equal(await page.evaluate(()=>localStorage.getItem('tplug_access_token')),null);
    await page.locator('[name=senha]').fill('NovaSenha123!');await page.locator('[name=confirmacao]').fill('NovaSenha123!');
    await page.getByRole('button',{name:'Atualizar senha →'}).click();
    await page.getByText('O link é inválido ou expirou.',{exact:false}).waitFor();
    assert.equal(await page.locator('app-reenviar-ativacao').count(),0);
    code=204; await page.getByRole('button',{name:'Atualizar senha →'}).click();
    await page.getByRole('heading',{name:'Acesse seu ERP'}).waitFor();
    assert.deepEqual(payload,{token:'r'.repeat(43),novaSenha:'NovaSenha123!'});
    assert.equal(await page.locator('#login-company').inputValue(),'0042');
    await context.close();
  }
  const context=await browser.newContext(); const page=await context.newPage();
  await page.goto(`${base}/teste`);
  await page.evaluate(()=>navigator.serviceWorker.ready);
  await page.reload();
  const manifest=await page.evaluate(async()=>await (await fetch('/manifest.webmanifest')).json());
  assert.equal(manifest.display,'standalone'); assert.equal(manifest.scope,'/');
  await context.setOffline(true); await page.reload();
  await page.getByRole('heading',{name:'Comece seu teste gratuito'}).waitFor();
  await fill(page); await page.locator('.create').click();
  await page.getByRole('alert').filter({hasText:'Não foi possível iniciar'}).waitFor();
  assert.equal(await page.getByRole('heading',{name:'Seu teste começou!'}).count(),0);
  assert.equal(await page.evaluate(async()=>!!(await caches.match('/api/public/trials'))),false);
  await context.close();
  console.log('Trial: sete larguras sem sobreposição; validação, payload, erros, retry, idempotência, ativação e PWA offline verificados.');
} finally {await browser.close();}
