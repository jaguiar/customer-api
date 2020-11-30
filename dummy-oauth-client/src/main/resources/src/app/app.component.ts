import {Component} from '@angular/core';

@Component({
    selector: 'app-root',
    template: `<div class="jumbotron text-center" style="margin-bottom:0">
        <h1><small><a href="/">Basic Customer Client App</a></small></h1>
  </div>
<router-outlet></router-outlet>`
})

export class AppComponent {}
