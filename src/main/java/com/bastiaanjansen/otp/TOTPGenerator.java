package com.bastiaanjansen.otp;

import com.bastiaanjansen.otp.helpers.URIHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Generates time-based one-time passwords
 *
 * @author Bastiaan Jansen
 * @see OTPGenerator
 */
public class TOTPGenerator extends OTPGenerator {
    private final static String OTP_TYPE = "totp";

    /**
     * Time interval between new codes
     */
    private final Duration period;

    /**
     * Constructs generator with custom password length, time interval and hashing algorithm
     *
     * @param passwordLength number of digits for generated code in range 6...8
     * @param period time interval between new codes
     * @param algorithm HMAC hash algorithm used to hash data
     * @param secret used to generate hash
     */
    public TOTPGenerator(final int passwordLength, final Duration period, final HMACAlgorithm algorithm, final byte[] secret) {
        super(passwordLength, algorithm, secret);

        if (period.getSeconds() < 1) throw new IllegalArgumentException("Period must be at least 1 second");

        this.period = period;
    }

    private TOTPGenerator(final TOTPGenerator.Builder builder) {
        super(builder.getPasswordLength(), builder.getAlgorithm(), builder.getSecret());
        this.period = builder.getPeriod();
    }

    /**
     * Generate a time-based one-time password for current time interval instant
     *
     * @return generated TOTP code
     * @throws IllegalStateException when code could not be generated
     */
    public String generate() throws IllegalStateException {
        long counter = calculateCounter(period);
        return super.generateCode(counter);
    }

    /**
     * Generate a time-based one-time password for a Java instant
     *
     * @param instant an instant
     * @return generated TOTP code
     * @throws IllegalStateException when code could not be generated
     */
    public String generate(final Instant instant) throws IllegalStateException {
        return generate(instant.getEpochSecond());
    }

    /**
     * Generate a time-based one-time password for a specific date
     *
     * @param date specific date
     * @return generated TOTP code
     * @throws IllegalStateException when code could not be generated
     */
    public String generate(final Date date) throws IllegalStateException {
        long secondsSince1970 = TimeUnit.MILLISECONDS.toSeconds(date.getTime());
        return generate(secondsSince1970);
    }

    /**
     * Generate a time-based one-time password for a specific time based on seconds past 1970
     *
     * @param secondsPast1970 seconds past 1970
     * @return generated TOTP code
     * @throws IllegalArgumentException when code could not be generated
     */
    public String generate(final long secondsPast1970) throws IllegalArgumentException {
        if (!validateTime(secondsPast1970))
            throw new IllegalArgumentException("Time must be above zero");

        long counter = calculateCounter(secondsPast1970, period);
        return super.generateCode(counter);
    }

    /**
     * Checks whether a code is valid for a specific counter
     *
     * @param code an OTP code
     * @return a boolean, true if code is valid, otherwise false
     */
    public boolean verify(final String code) {
        long counter = calculateCounter(period);
        return super.verify(code, counter);
    }

    /**
     * Checks whether a code is valid for a specific counter taking a delay window into account
     *
     * @param code an OTP code
     * @param delayWindow window in which a code can still be deemed valid
     * @return a boolean, true if code is valid, otherwise false
     */
    public boolean verify(final String code, final int delayWindow) {
        long counter = calculateCounter(period);
        return super.verify(code, counter, delayWindow);
    }

    public Duration getPeriod() {
        return period;
    }

    /**
     * Create a OTPAuth URI for easy on-boarding with only an issuer
     *
     * @param issuer name
     * @return generated OTPAuth URI
     * @throws URISyntaxException when URI cannot be created
     */
    public URI getURI(final String issuer) throws URISyntaxException {
        return getURI(issuer, "");
    }

    /**
     * Create a OTPAuth URI for easy user on-boarding with an issuer and account name
     *
     * @param issuer name
     * @param account name
     * @return generated OTPAuth URI
     * @throws URISyntaxException when URI cannot be created
     */
    public URI getURI(final String issuer, final String account) throws URISyntaxException {
        Map<String, String> query = new HashMap<>();
        query.put(URIHelper.PERIOD, String.valueOf(period.getSeconds()));

        return getURI(OTP_TYPE, issuer, account, query);
    }

    /**
     * Calculate counter for a specific time in seconds past 1970 and time interval
     *
     * @param secondsPast1970 seconds past 1970
     * @param period time interval between new codes
     * @return counter based on seconds past 1970 and time interval
     */
    private long calculateCounter(final long secondsPast1970, final Duration period) {
        return TimeUnit.SECONDS.toMillis(secondsPast1970) / period.toMillis();
    }

    /**
     * Calculate counter based on current time and a specific time interval
     *
     * @param period time interval between new codes
     * @return counter based on current time and a specific time interval
     */
    private long calculateCounter(final Duration period) {
        return System.currentTimeMillis() / period.toMillis();
    }

    /**
     * Check if time is above zero
     *
     * @param time time value to check against
     * @return whether time is above zero
     */
    private boolean validateTime(final long time) {
        return time > 0;
    }


    /**
     * @author Bastiaan Jansen
     * @see TOTPGenerator
     */
    public static class Builder extends OTPGenerator.Builder<Builder, TOTPGenerator>  {
        /**
         * Time interval between new codes
         */
        private Duration period;

        /**
         * Default time interval for a time-based one-time password
         */
        public static final Duration DEFAULT_PERIOD = Duration.ofSeconds(30);

        /**
         * Constructs a TOTPGenerator builder
         *
         * @param secret used to generate hash
         */
        public Builder(byte[] secret) {
            super(secret);
            this.period = DEFAULT_PERIOD;
        }

        private Builder(final URI uri) throws URISyntaxException {
            super(uri);

            Map<String, String> query = URIHelper.queryItems(uri);

            try {
                this.period = Optional.ofNullable(query.get(URIHelper.PERIOD))
                        .map(Long::parseLong).map(Duration::ofSeconds)
                        .orElse(DEFAULT_PERIOD);
            } catch (Exception e) {
                throw new URISyntaxException(uri.toString(), "URI could not be parsed");
            }
        }

        /**
         * Change period
         *
         * @param period time interval between new codes
         * @return builder
         */
        public Builder withPeriod(Duration period) {
            if (period.getSeconds() < 1) throw new IllegalArgumentException("Period must be at least 1 second");
            this.period = period;
            return this;
        }

        public Duration getPeriod() {
            return period;
        }

        /**
         * Build the generator with specified options
         *
         * @return TOTPGenerator
         */
        @Override
        public TOTPGenerator build() {
            return new TOTPGenerator(this);
        }

        @Override
        public Builder getBuilder() {
            return this;
        }

        /**
         * Build a TOTPGenerator from an OTPAuth URI
         *
         * @param uri OTPAuth URI
         * @return TOTPGenerator
         * @throws URISyntaxException when URI cannot be parsed
         */
        public static TOTPGenerator fromOTPAuthURI(final URI uri) throws URISyntaxException {
            return new TOTPGenerator.Builder(uri).build();
        }

        /**
         * Create a TOTPGenerator with default values
         *
         * @param secret used to generate hash
         * @return a TOTPGenerator with default values
         */
        public static TOTPGenerator withDefaultValues(final byte[] secret) {
            return new TOTPGenerator.Builder(secret).build();
        }
    }
}
