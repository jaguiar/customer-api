import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import { Token } from '../models/Authentication.model';

@Injectable()
export class AuthenticationService {
   private clientId = 'randomClientId';
   private redirectUri = 'http://localhost:8089/';

  constructor(
    private _http: HttpClient,
    private cookieService: CookieService){}

  retrieveToken(code: string){
    let params = new URLSearchParams();
    params.append('grant_type','authorization_code');
    params.append('client_id', this.clientId);
    params.append('redirect_uri', this.redirectUri);
    params.append('code',code);

    let headers = new HttpHeaders({
      'Content-type': 'application/x-www-form-urlencoded; charset=utf-8',
      'Authorization': 'Basic '+btoa(this.clientId+":DontDoThisAtHome")
    });
    return this._http.post('http://localhost:8081/shady-authorization-server/oauth/token', 
      params.toString(), { headers: headers });
  }

  saveToken(token: Token){
    var expireDate = new Date().getTime() + (1000 * token.expires_in);
    this.cookieService.set("access_token", token.access_token, expireDate);
    this.cookieService.set("user_name", token.sub, expireDate);
  }

  checkCredentials(){
    return this.cookieService.check('access_token');
  }

  getLoggedUser(){
      return this.cookieService.get('user_name');
  }

  logout() {
    this.cookieService.delete('access_token');
    this.cookieService.delete('user_name');
    window.location.reload();
  }

}
