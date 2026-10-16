import {Component,Input} from '@angular/core';
@Component({selector:'ui-icon',standalone:true,template:`<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path [attr.d]="paths[name] || paths['file']"/></svg>`,styles:[`:host{display:inline-flex;width:1.35em;height:1.35em;flex:none;vertical-align:middle}svg{width:100%;height:100%}`]})
export class UiIconComponent {
 @Input() name='file';
 readonly paths:Record<string,string>={
 home:'M3 11 12 3l9 8M5 10v11h5v-7h4v7h5V10',
 users:'M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M2 21v-3a7 7 0 0 1 14 0v3M17 3a4 4 0 0 1 0 8M18 15a5 5 0 0 1 4 5v1',
 clipboard:'M8 4H4v18h16V4h-4M8 2h8v5H8zM8 13l3 3 5-6',
 wallet:'M4 6V3h14v4M3 7h18v14H3zM16 12h5v5h-5z',
 money:'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20M15 7h-4a2.5 2.5 0 0 0 0 5h2a2.5 2.5 0 0 1 0 5H9M12 5v14',
 file:'M5 2h9l5 5v15H5zM14 2v6h5M9 12h6M9 16h6',
 shield:'m12 2 9 4v6c0 5-9 10-9 10S3 17 3 12V6l9-4M8 12l3 3 5-6',
 support:'M4 14v-3a8 8 0 0 1 16 0v3M4 12H2v7h4v-7zM20 12h2v7h-4v-7zM20 19v3h-7',
 bell:'M4 18h16l-2-4V9a6 6 0 0 0-12 0v5l-2 4M10 22h4',
 chart:'M3 21V12h4v9M10 21V6h4v15M17 21V2h4v19',
 settings:'M9 3h6l1 3 3 1 2 5-2 5-3 1-1 3H9l-1-3-3-1-2-5 2-5 3-1 1-3M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8',
 search:'M10 3a7 7 0 1 0 0 14 7 7 0 0 0 0-14M15 15l6 6',
 menu:'M4 6h16M4 12h16M4 18h16',
 down:'M6 9l6 6 6-6',
 export:'M12 2v13M7 10l5 5 5-5M4 17v5h16v-5',
 plus:'M12 4v16M4 12h16',
 flask:'M9 2h6M10 2v7L4 19a2 2 0 0 0 2 3h12a2 2 0 0 0 2-3L14 9V2M7 16h10M11 12h.01M13 18h.01',
 alert:'m12 2 10 19H2L12 2M12 8v6M12 17h.01',
 cancel:'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20M8 8l8 8M16 8l-8 8',
 info:'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20M12 11v6M12 7h.01',
 store:'M3 9h18l-2-6H5L3 9M4 9v12h16V9M8 21v-7h8v7M3 9a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0',
 monitor:'M2 3h20v14H2zM12 17v5M7 22h10',
 calendar:'M3 5h18v17H3zM7 2v6M17 2v6M3 11h18M7 15h3M14 15h3M7 19h3',
 clock:'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20M12 6v7l4 2',
 edit:'m4 16 12-12 4 4L8 20l-5 1 1-5M14 6l4 4',
 send:'m22 2-7 20-4-9-9-4 20-7M11 13 11 11',
 lock:'M5 10h14v12H5zM8 10V6a4 4 0 0 1 8 0v4M12 15v3',
 spark:'m12 2 2.5 7.5L22 12l-7.5 2.5L12 22l-2.5-7.5L2 12l7.5-2.5L12 2',
 phone:'M7 3 3 5c0 8 8 16 16 16l2-4-5-3-2 2-6-6 2-2-3-5',
 };
}
