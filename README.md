# OTP-Java

![](https://github.com/BastiaanJansen/OTP-Java/workflows/test/badge.svg)
![](https://github.com/BastiaanJansen/OTP-Java/workflows/build/badge.svg)
![](https://img.shields.io/github/license/BastiaanJansen/OTP-Java)
![](https://img.shields.io/github/issues/BastiaanJansen/OTP-Java)

A small and easy-to-use one-time password generator for Java according to [RFC 4226](https://tools.ietf.org/html/rfc4226) (HOTP) and [RFC 6238](https://tools.ietf.org/html/rfc6238) (TOTP).

## Usage
### HOTP (Counter-based one-time passwords)
##### Generation of HOTP object:

```java
String secret = "VV3KOX7UQJ4KYAKOHMZPPH3US4CJIMH6F3ZKNB5C2OOBQ6V2KIYHM27Q";
HOTPGenerator hotp = new HOTPGenerator(secret);
```

The default length of a generated code is six digits. You can change the default:
```java
String secret = "ABCDEFGHIJKLMNOP";
int passwordLength = 6;
HOTPGenerator hotp = new HOTPGenerator(passwordLength, secret);

// It is also possible to create a HOTPGenerator instance based on an OTPAuth URI. When algorithm or digits are not specified, the default values will be used.
new HOTPGenerator("otpauth://hotp/issuer?secret=ABCDEFGHIJKLMNOP&algorithm=SHA1&digits=6&counter=8237");
```

Get information about the generator:

```java
String secret = hotp.getSecret(); // VV3KOX7UQJ4KYAKOHMZPPH3US4CJIMH6F3ZKNB5C2OOBQ6V2KIYHM27Q
int passwordLength = hotp.getPasswordLength(); // 6
HMACAlgorithm algorithm = hotp.getAlgorithm(); // HMACAlgorithm.SHA1
```

##### Generation of HOTP code:
After creating an instance of the HOTPGenerator class, a code can be generated by using the `generate()` method:
```java
try {
    int counter = 5;
    String code = hotp.generate(counter);
    
    // To verify a token:
    boolean isValid = hotp.verify(code, counter);
    
    // Or verify with a delay window
    boolean isValid = hotp.verify(code, counter, 2);
} catch (IllegalStateException e) {
    // Handle error
}
```

### TOTP (Time-based one-time passwords)
##### Generation of TOTP object:
TOTPGenerator can accept more paramaters: `passwordLength`, `period`, `algorithm` and `secret`. The default values are: passwordLength = 6, period = 30, algorithm = SHA1.

```java
String secret = "VV3KOX7UQJ4KYAKOHMZPPH3US4CJIMH6F3ZKNB5C2OOBQ6V2KIYHM27Q";
int passwordLength = 8; // Password length must be between 6 and 8
Duration period = Duration.ofSeconds(30); // This can of course be any period
HMACAlgorithm algorithm = HMACAlgorithm.SHA1; // SHA256 and SHA512 are also supported

TOTPGenerator totp = new TOTPGenerator(passwordLength, period, algorithm, secret);
```

Get information about the generator:
```java
String secret = totp.getSecret(); // VV3KOX7UQJ4KYAKOHMZPPH3US4CJIMH6F3ZKNB5C2OOBQ6V2KIYHM27Q
int passwordLength = totp.getPasswordLength(); // 6
HMACAlgorithm algorithm = totp.getAlgorithm(); // HMACAlgorithm.SHA1
Duration period = totp.getPeriod(); // Duration.ofSeconds(30)
```

##### Generation of TOTP code:
After creating an instance of the TOTPGenerator class, a code can be generated by using the `generate()` method, similarly with the HOTPGenerator class:
```java
try {
    String code = totp.generate();
     
    // To verify a token:
    boolean isValid = totp.verify(code);
} catch (IllegalStateException e) {
    // Handle error
}
```
The above code will generate a time-based one-time password based on the current time. The API supports, besides the current time, the creation of codes based on `timeSince1970` in milliseconds, `Date`, `Instant` and `URI` values:

```java
try {
    // Based on current time
    totp.generate();
    
    // Based on specific date
    totp.generate(new Date());
    
    // Based on milliseconds past 1970
    totp.generate(9238346823);
    
    // Based on an instant
    totp.generate(Instant.now());
    
    // Based on an OTPAuth URI. When algorithm, period or digits are not specified, the default values will be used
    totp.generate(new URI("otpauth://totp/issuer?secret=ABCDEFGHIJKLMNOP&algorithm=SHA1&digits=6&period=30"));
} catch (IllegalStateException e) {
    // Handle error
}
```

## Licence
OTP-Java is available under the MIT licence. See the LICENCE for more info.
