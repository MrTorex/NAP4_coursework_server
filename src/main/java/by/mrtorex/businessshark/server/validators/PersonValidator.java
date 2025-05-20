package by.mrtorex.businessshark.server.validators;

import by.mrtorex.businessshark.server.interfaces.Validatable;
import by.mrtorex.businessshark.server.model.entities.Person;

/**
 * Валидатор для проверки корректности объекта Person.
 */
public class PersonValidator implements Validatable<Person> {

    /**
     * Проверяет, что объект Person не null и содержит непустые имя и фамилию.
     *
     * @param person проверяемый объект Person
     * @return true, если объект корректен; false в противном случае
     */
    @Override
    public boolean isValid(Person person) {
        return person != null &&
                person.getFirstName() != null &&
                !person.getFirstName().isEmpty() &&
                person.getLastName() != null &&
                !person.getLastName().isEmpty();
    }
}
