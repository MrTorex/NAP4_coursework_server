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
 * Сущность, представляющая компанию.
 * Используется для хранения и обработки информации о компаниях в базе данных.
 */
@Entity
@Table(name = "Companies")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Company {

    /**
     * Уникальный идентификатор компании.
     * Генерируется автоматически.
     */
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Название компании.
     * Не может быть null. Максимальная длина — 50 символов.
     */
    @Expose
    @Column(name = "name", nullable = false, length = 50)
    private String name;

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
        Company company = (Company) o;
        return getId() != null && Objects.equals(getId(), company.getId());
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
