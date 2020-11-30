export class Token {
    constructor(
      public sub: string,
      public expires_in: number,
      public access_token : string
    ) {}
  }