import { readFileSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const dist=resolve('dist');
let html=readFileSync(resolve(dist,'index.html'),'utf8');
const css=readFileSync(resolve(dist,'styles.css'),'utf8');
const polyfills=readFileSync(resolve(dist,'polyfills.js'),'utf8');
let main=readFileSync(resolve(dist,'main.js'),'utf8');
const screen='data:image/webp;base64,'+readFileSync(resolve(dist,'assets/ui.webp')).toString('base64');
main=main.replaceAll('assets/ui.webp',screen);
html=html.replace(/<link rel="stylesheet" href="styles\.css"[^>]*><noscript><link rel="stylesheet" href="styles\.css"><\/noscript>/,'<style>'+css+'</style>');
html=html.replace('<script src="polyfills.js" type="module"></script>','<script type="module">'+polyfills+'</script>');
html=html.replace('<script src="main.js" type="module"></script>','<script type="module">'+main+'</script>');
writeFileSync(resolve('TRAXUP-UI001-Dashboard-Prototipo.html'),html);
console.log(resolve('TRAXUP-UI001-Dashboard-Prototipo.html'));
