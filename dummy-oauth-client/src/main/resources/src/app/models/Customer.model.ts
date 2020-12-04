export class Customer {
  constructor(
    public customerId: string,
    public lastName? : string,
    public firstName?: string,
    public email?: string,
    public phoneNumber?: string
  ) {}
}

export class CustomerPreferencesProfile {
  constructor(
    public profileName: string,
    public seatPreference?: string,
    public classPreference?: string,
    public language?: string,
    public id?
  ) {}
}

export class SaveCustomerPreferencesRequest {
  constructor(
    public profileName: string,
    public seatPreference? : string,
    public classPreference? : string,
    public language?: string,
    public id?: string
  ) {}
}

