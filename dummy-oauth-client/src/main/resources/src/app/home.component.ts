import { Component } from '@angular/core';
import { Token } from './models/Authentication.model';
import { AuthenticationService } from './services/auth.service';
import { CustomerService } from './services/customer.service';

@Component({
  selector: 'home-header',
  providers: [AuthenticationService, CustomerService],
  templateUrl: './templates/home.html'
})
export class HomeComponent {
     private clientId = 'randomClientId';
     private redirectUri = 'http://localhost:8089/';

     public isLoggedIn = false;
     public isReady = false;
     public user: string;
     public applicationType: string = 'Oops';

    constructor(
        private _authentService: AuthenticationService,
        private _customerService: CustomerService){}

    ngOnInit(){
        this.isReady = false;
        this.isLoggedIn = this._authentService.checkCredentials();
        this.user = this._authentService.getLoggedUser();
        console.log('user_name', this.user);
        let i = window.location.href.indexOf('code');
        this._customerService.getCustomerVersion()
            .subscribe(data => {
                console.log('Customer version retrieved sucessfully', data);
                if (data && data['applicationType']) {
                    this.applicationType = data['applicationType'];
                }
            }, err => {
                console.log('Customer version error', err);
            });
        if(!this.isLoggedIn && i != -1) { // got authorization code => can retrieve access token
            this._authentService.retrieveToken(window.location.href.substring(i + 5))
                .subscribe(
                      data => {
                          console.log("Retrieved tokeninfo : ", data);
                          this._authentService.saveToken(new Token(data['sub'], data['expires_in'], data['access_token']));
                          this.user = data['sub'];
                          window.location.href = 'http://localhost:8089';
                      },
                      err => {
                        this.isReady = true;
                        alert('Invalid Credentials')
                      });
        } else {
          this.isReady = true;
        }
    }

    login() {
        window.location.href = 'http://localhost:8081/shady-authorization-server/oauth/authorize?response_type=code&client_id='
        + this.clientId + '&redirect_uri='+ this.redirectUri;
    }

    logout() {
        this._authentService.logout();
    }
}
