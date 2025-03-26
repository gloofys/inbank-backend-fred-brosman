package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.AgeRestrictionException;
import org.springframework.stereotype.Service;


@Service
public class AgeValidator {

    private final EstonianPersonalCodeParser parser = new EstonianPersonalCodeParser();

    public void validate(String personalCode) throws AgeRestrictionException {
        int age;

        try {
            age = parser.getAge(personalCode).getYears();
        } catch (PersonalCodeException e) {
            throw new AgeRestrictionException("Invalid personal code: " + e.getMessage());
        }

        int lifeExpectancy = getLifeExpectancyFromCountry(personalCode);
        int maxAllowedAge = lifeExpectancy - DecisionEngineConstants.MAXIMUM_LOAN_PERIOD_IN_YEARS;

        if (age < 18 || age > maxAllowedAge) {
            throw new AgeRestrictionException("Loan not allowed due to age restrictions.");
        }
    }

    private static int getLifeExpectancyFromCountry(String personalCode) {
        // NOTE: For ticket-102, all personal codes follow the same format and expected lifetimes are arbitrary.
        // In a real scenario, country information would be extracted from metadata or code prefix. Mocking country logic as part of Baltic scope.

        // TODO: Replace with actual country detection logic if needed
        // Currently defaulting to Estonia for all cases
        return DecisionEngineConstants.ESTONIA_LIFE_EXPECTANCY;

//    Example if real country detection was implemented with country code prefix:
//    if (country == "EE") return DecisionEngineConstants.ESTONIA_LIFE_EXPECTANCY;
//    if (country == "LV") return DecisionEngineConstants.LATVIA_LIFE_EXPECTANCY;
//    if (country == "LT") return DecisionEngineConstants.LITHUANIA_LIFE_EXPECTANCY;
//
    }
}
