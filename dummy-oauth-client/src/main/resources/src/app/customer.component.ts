import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CustomerService } from './services/customer.service'
import { Customer, SaveCustomerPreferencesRequest, CustomerPreferencesProfile } from './models/Customer.model'

@Component({
  selector: 'customer-details',
  providers: [CustomerService],
  templateUrl: './templates/customer.html'
})
export class CustomerComponent implements OnInit {
  private customerUrl = 'http://localhost:4600/customers';
  customer: Customer = new Customer('');
  currentCustomerProfile: CustomerPreferencesProfile = new CustomerPreferencesProfile('');
  displaySuccessMsg: boolean = false;
  userForm: FormGroup;
  errorMessage: string;
  warningMessage: string;
  allProfiles: CustomerPreferencesProfile[] = [];

  constructor(private _service:CustomerService,
    private formBuilder: FormBuilder) {}

  ngOnInit() {
    this.initForm();
    this.getCustomer();
    this._service.customerPrefSubject.subscribe(
      (profileData: CustomerPreferencesProfile) => {
        if (profileData) {
          this.setFormValue('selectedProfile', profileData.profileName);
          this.setFormValue('profileName', profileData.profileName);
          this.setFormValue('seatPreference', profileData.seatPreference);
          this.setFormValue('classPreference', profileData.classPreference);
          this.setFormValue('language', profileData.language);
        } else {
          console.log('No preferences set');
        }
      }
    );
    this._service.savedPrefsResultEvent.subscribe(isSaved => {this.displaySuccessMsg = isSaved});
    this._service.customerPrefSubject.subscribe(lastCreated => this.allProfiles.push(lastCreated));
    this._service.customerErrorEvent.subscribe(errMsg => this.errorMessage = errMsg);
    this._service.allProfiles.subscribe(profiles =>  this.allProfiles = profiles);
    this._service.customerWarningEvent.subscribe(warnMsg => this.warningMessage = warnMsg);
    this._service.getCustomerPreferences();
  }

  initForm() {
    this.userForm = this.formBuilder.group({
      profileName: ['', [Validators.required, Validators.pattern('[0-9a-zA-Z ]+')]],
      seatPreference: '',
      classPreference: ['', Validators.pattern('(1|2)')],
      language: 'fr',
      selectedProfile: ''
    });
  }

  getCustomer() {
    this._service.getCustomer(this.customerUrl)
      .subscribe(
        data => {
          this.customer = data;
          console.log('Obtained Customer:', data);
        },
        _ =>  this.customer = new Customer(''));
  }

  closeMessage() {
    this.displaySuccessMsg = false;
    this.errorMessage = undefined;
    this.warningMessage = undefined;
  }

  onSubmitForm() {
    const formValue = this.userForm.value;
    const toCreate = new SaveCustomerPreferencesRequest(
      formValue['profileName'],
      formValue['seatPreference'],
      formValue['classPreference'],
      formValue['language'],
      formValue['id']
    );
    this._service.createCustomerPreferences(toCreate);
  }

  onProfileChange(){
    const formValue = this.userForm.value['selectedProfile'];
    let found = this.allProfiles.filter(e => e.id === formValue);
    
    if (found && found.length > 0) {
      this.setFormValue('profileName', found[0].profileName);
      this.setFormValue('seatPreference', found[0].seatPreference);
      this.setFormValue('classPreference', found[0].classPreference);
      this.setFormValue('language', found[0].language);
    }
  }

  private setFormValue(fieldName: string, fieldValue: string) {
    this.userForm.controls[fieldName].setValue(fieldValue);
  }
}
