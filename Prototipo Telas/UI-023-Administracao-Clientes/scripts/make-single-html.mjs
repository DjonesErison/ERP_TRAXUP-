import {readFileSync,writeFileSync} from 'node:fs';
let html=readFileSync('dist/index.html','utf8');const css=readFileSync('dist/styles.css','utf8');const logo='data:image/png;base64,'+readFileSync('src/assets/Logo.png').toString('base64');
const main=readFileSync('dist/main.js','utf8').replaceAll('assets/Logo.png',logo);
html=html.replace(/<link rel="stylesheet"[^>]*href="styles\.css"[^>]*>/g,()=>'<style>'+css+'</style>').replace(/<noscript>.*?<\/noscript>/gs,'');
for(const [file,code] of [['polyfills.js',readFileSync('dist/polyfills.js','utf8')],['main.js',main]])html=html.replace(`<script src="${file}" type="module"></script>`,()=>'<script type="module">'+code.replaceAll('</script','<\\/script')+'</script>');
if(/(?:src|href)="(?:assets\/|main\.js|polyfills\.js|styles\.css)/.test(html))throw Error('Dependência externa não incorporada');
writeFileSync('TRAXUP-UI023-Administracao-Clientes.html',html);
console.log('HTML independente gerado.');
