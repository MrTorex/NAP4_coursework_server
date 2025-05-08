package by.mrtorex.businessshark.server.validators;

import by.mrtorex.businessshark.server.interfaces.Validatable;
import by.mrtorex.businessshark.server.model.entities.Person;

public class PersonValidator implements Validatable<Person> {

    @Override
    public boolean isValid(Person person) {
        return person != null &&
                person.getFirstName() != null &&
                !person.getFirstName().isEmpty() &&
                person.getLastName() != null &&
                !person.getLastName().isEmpty();
    }
}

