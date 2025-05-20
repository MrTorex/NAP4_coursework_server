package by.mrtorex.businessshark.server.model.entities;

import com.google.gson.annotations.Expose;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Сущность, представляющая физическое лицо.
 * Используется для хранения информации о пользователях, клиентах или сотрудниках.
 */
@Entity
@Table(name = "Persons")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Person {

    /**
     * Уникальный идентификатор физического лица.
     * Генерируется автоматически.
     */
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Имя физического лица.
     * Не может быть null. Максимальная длина — 50 символов.
     */
    @Expose
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * Отчество физического лица.
     * Может быть null. Максимальная длина — 50 символов.
     */
    @Expose
    @Column(name = "patronymic", length = 50)
    private String patronymic;

    /**
     * Фамилия физического лица.
     * Не может быть null. Максимальная длина — 50 символов.
     */
    @Expose
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Переопределённый метод сравнения объектов на равенство.
     * Учитывает возможную прокси-обёртку Hibernate.
     *
     * @param o сравниваемый объект
     * @return true, если объекты эквивалентны; false в противном случае
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
                o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
                this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Person person = (Person) o;
        return getId() != null && Objects.equals(getId(), person.getId());
    }

    /**
     * Переопределённый метод генерации хеш-кода.
     * Учитывает возможную прокси-обёртку Hibernate.
     *
     * @return хеш-код объекта
     */
    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() :
                getClass().hashCode();
    }
}
