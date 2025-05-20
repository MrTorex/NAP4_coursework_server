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
 * Сущность, представляющая пользователя системы.
 * Хранит данные для аутентификации и связывает пользователя с ролью и персональными данными.
 */
@Entity
@Table(name = "Users")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User {

    /**
     * Уникальный идентификатор пользователя.
     * Генерируется автоматически при сохранении в базе.
     */
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Уникальное имя пользователя (логин).
     * Не может быть null. Максимальная длина — 50 символов.
     */
    @Expose
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Хеш пароля пользователя.
     * Не может быть null. Длина фиксированная — 64 символа (например, SHA-256).
     */
    @Expose
    @Column(name = "password_hash", nullable = false, length = 64)
    private String passwordHash;

    /**
     * Роль пользователя в системе.
     * Обязательное поле, загружается жадно (EAGER).
     */
    @Expose
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Связанные персональные данные пользователя.
     * Обязательное поле, уникально для каждого пользователя.
     */
    @Expose
    @OneToOne
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    /**
     * Переопределённый метод сравнения объектов на равенство.
     * Учитывает прокси-обёртку Hibernate для корректного сравнения.
     *
     * @param o объект для сравнения
     * @return true, если объекты одного типа и имеют одинаковый идентификатор
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
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    /**
     * Переопределённый метод генерации хеш-кода.
     * Учитывает прокси-обёртку Hibernate.
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
