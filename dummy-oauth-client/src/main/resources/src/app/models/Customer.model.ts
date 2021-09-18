export class Customer {
  constructor(
    public customerId: string,
    public lastName? : string,
    public firstName?: string,
    public email?: string,
    public phoneNumber?: string,
    public age?: number,
    public loyaltyProgram?: LoyaltyProgram
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

export class LoyaltyProgram {
  constructor(
    public number: string,
    public label : string
  ) {}
}

