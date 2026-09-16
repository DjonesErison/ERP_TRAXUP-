import { Component, Input } from '@angular/core';

// Local vector icons keep navigation consistent across desktop and mobile fonts.
@Component({
  selector: 'app-icon',
  standalone: true,
  template: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path [attr.d]="paths[name] || paths['grid']" /></svg>`,
  styles: [':host { display: inline-flex; flex: 0 0 auto; width: 1.5em; height: 1.5em; } svg { width: 100%; height: 100%; }']
})
export class UiIconComponent {
  @Input() name = 'grid';
  readonly paths: Record<string, string> = {
    home: 'M3 10 12 3l9 7M5 9v12h5v-7h4v7h5V9',
    cart: 'M2 3h3l3 12h11l3-9H6M9 19h.01M18 19h.01M8 19a1 1 0 1 0 2 0 1 1 0 0 0-2 0M17 19a1 1 0 1 0 2 0 1 1 0 0 0-2 0',
    box: 'm12 2 9 5v10l-9 5-9-5V7l9-5ZM3 7l9 5 9-5M12 12v10M7 4.8l10 5.5',
    stock: 'M3 4h18v4H3zM5 8v13h14V8M10 12h4',
    users: 'M9 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM2 21v-2a7 7 0 0 1 14 0v2M17 4a4 4 0 0 1 0 8M18 15a5 5 0 0 1 4 5v1',
    user: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM4 22v-2a8 8 0 0 1 16 0v2',
    money: 'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20ZM15 8h-4a2 2 0 0 0 0 4h2a2 2 0 0 1 0 4H9M12 6v12',
    file: 'M5 2h9l5 5v15H5zM14 2v6h5M9 12h6M9 16h6',
    chart: 'M4 21V11h3v10M11 21V3h3v18M18 21V7h3v14',
    spark: 'm12 2 2.5 7.5L22 12l-7.5 2.5L12 22l-2.5-7.5L2 12l7.5-2.5L12 2Z',
    settings: 'M9 3h6l1 3 3 1 2 5-2 5-3 1-1 3H9l-1-3-3-1-2-5 2-5 3-1 1-3ZM12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z',
    shield: 'm12 2 9 4v6c0 5-9 10-9 10S3 17 3 12V6l9-4ZM8 12l3 3 5-6',
    cloud: 'M7 19a5 5 0 0 1-1-10 6 6 0 0 1 12-1 5.5 5.5 0 0 1 0 11H7Z',
    menu: 'M4 6h16M4 12h16M4 18h16',
    search: 'M10 3a7 7 0 1 0 0 14 7 7 0 0 0 0-14ZM15 15l6 6',
    lock: 'M5 10h14v12H5zM8 10V6a4 4 0 0 1 8 0v4M12 15v3',
    eye: 'M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12ZM12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z',
    arrow: 'M4 12h16M14 6l6 6-6 6',
    down: 'M6 9l6 6 6-6',
    monitor: 'M2 3h20v14H2zM12 17v4M7 21h10',
    download: 'M12 2v14M6 10l6 6 6-6M4 17v5h16v-5',
    help: 'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20ZM9 8a3 3 0 0 1 6 0c0 2-3 2-3 5M12 17h.01',
    bell: 'M4 17h16l-2-4V8a6 6 0 0 0-12 0v5l-2 4ZM10 21h4',
    building: 'M4 22V2h12v20M16 10h5v12M8 6h4M8 10h4M8 14h4M9 22v-4h3v4',
    grid: 'M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z'
  };
}
