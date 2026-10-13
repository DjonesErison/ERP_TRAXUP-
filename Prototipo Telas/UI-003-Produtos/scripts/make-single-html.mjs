import { readFileSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const dist=resolve('dist');
let html=readFileSync(resolve(dist,'index.html'),'utf8');
const css=readFileSync(resolve(dist,'styles.css'),'utf8');
const polyfills=readFileSync(resolve(dist,'polyfills.js'),'utf8');
let main=readFileSync(resolve(dist,'main.js'),'utf8');
const logo='data:image/webp;base64,'+readFileSync(resolve(dist,'assets/traxup-logo.webp')).toString('base64');
const sprite=readFileSync(resolve(dist,'assets/products.svg'),'utf8').replace('<svg ','<svg style="display:none" aria-hidden="true" ');
main=main.replaceAll('assets/traxup-logo.webp',logo).replaceAll('assets/products.svg#','#');
html=html.replace(/<link rel="stylesheet" href="styles\.css"[^>]*><noscript><link rel="stylesheet" href="styles\.css"><\/noscript>/,'<style>'+css+'</style>');
html=html.replace('<script src="polyfills.js" type="module"></script>','<script type="module">'+polyfills+'</script>');
html=html.replace('<script src="main.js" type="module"></script>','<script type="module">'+main+'</script>');
html=html.replace('<body>','<body>'+sprite);
writeFileSync(resolve('TRAXUP-UI003-Produtos-Prototipo.html'),html);
console.log(resolve('TRAXUP-UI003-Produtos-Prototipo.html'));
