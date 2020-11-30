import { Injectable } from '@angular/core';
import { Cookie } from 'ng2-cookies';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import { CustomerPreferences, CustomerPreferencesProfile, SaveCustomerPreferencesRequest } from '../models/Customer.model';

@Injectable()
export class CustomerService {
  private baseUrl = 'http://localhost:4600/';
   private customerUrl = this.baseUrl + 'customers';
   private lastCreated: CustomerPreferencesProfile;
   private customerPreferences: CustomerPreferencesProfile[];
   customerPrefSubject = new Subject<CustomerPreferencesProfile>();
   savedPrefsResultEvent = new Subject<boolean>();
   customerErrorEvent = new Subject<string>();
   customerWarningEvent = new Subject<string>();
   allProfiles = new Subject<CustomerPreferencesProfile[]>();

  constructor(
    private _http: HttpClient){}

  getCustomerVersion(): Observable<any>{
    console.log('Retrieving authenticated customer');
    var headers = new HttpHeaders({'Accept': 'application/json',
    'Authorization': 'Bearer ' + Cookie.get('access_token'),
    'user-device-platform': 'ANDROID',
    'user-app-client-version': 'devoxx-2020'
    });
    return this._http.get(this.baseUrl + 'app.info', { headers: headers })
                   .catch((error:any) => Observable.throw(error.json().error || 'Server error'));
  }  

  getCustomer(resourceUrl: string) : Observable<any>{
    console.log('Retrieving authenticated customer');
    var headers = new HttpHeaders({'Content-type': 'application/x-www-form-urlencoded; charset=utf-8',
    'Authorization': 'Bearer ' + Cookie.get('access_token'),
    'user-device-platform': 'ANDROID',
    'user-app-client-version': 'devoxx-2020'
    });
    return this._http.get(resourceUrl, { headers: headers })
                   .catch((error:any) => Observable.throw(error.json().error || 'Server error'));
  }

  emitCustomerPreferencesProfile() {
    this.customerPrefSubject.next(this.lastCreated);
  }

  emitAllCustomerPreferences() {
    this.allProfiles.next(this.customerPreferences);
  }

  emitCustomerError(err) {
    if (err.error && err.error.code && err.error.message) {
      this.customerErrorEvent.next(err.error.code + ':' +err.error.message);
    }
    this.customerErrorEvent.next(err.status + ':' + err.message);
  }

  createCustomerPreferences(toCreate: SaveCustomerPreferencesRequest) {
    console.log('Calling createCustomerPreferences');
    const headers = new HttpHeaders({'Content-type': 'application/json; charset=utf-8',
        'Authorization': 'Bearer ' + Cookie.get('access_token'),
        'user-device-platform': 'ANDROID',
        'user-app-client-version': 'devoxx-2020'
        });
    const userName = Cookie.get('user_name');    
    const url = this.customerUrl + '/preferences' ;    
    this._http.post<CustomerPreferencesProfile>(url, toCreate, { headers: headers })
                   .subscribe(data => {
                      console.log('Customer preferences profile save succeeded', data);
                      this.lastCreated = data;
                      this.emitCustomerPreferencesProfile();
                      this.savedPrefsResultEvent.next(true);
                   },
                   err => {
                      console.log('Customer preferences profile save failed', err);
                      this.emitCustomerError(err);
                   });
  }

  getCustomerPreferences() {
    console.log('Calling getCustomerPreferences');
    const headers = new HttpHeaders({'Content-type': 'application/json; charset=utf-8',
        'Authorization': 'Bearer ' + Cookie.get('access_token'),
        'user-device-platform': 'ANDROID',
        'user-app-client-version': 'devoxx-2020'
        });
    const userName = Cookie.get('user_name');    
    const url = this.customerUrl + '/preferences';
    this._http.get<CustomerPreferences>(url, { headers: headers })
                   .subscribe(data => {
                      console.log('Customer preferences retrieved', data);
                      if (data && data.profiles && data.profiles.length > 0) {
                        console.log('data.profiles', data.profiles);
                        this.customerPreferences = data.profiles;
                        this.emitAllCustomerPreferences();
                      }
                   },
                   err => {
                      console.log('Failed to retrieve customer preferences', err);
                      if (err.error && err.error.code && err.error.message) {
                        this.customerWarningEvent.next(err.error.message);
                      } else {
                        this.customerWarningEvent.next(err.message);
                      }
                   });
  }
}
