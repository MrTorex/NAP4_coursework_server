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
 * Сущность, представляющая акцию на фондовом рынке.
 * Используется для хранения информации о биржевых ценных бумагах.
 */
@Entity
@Table(name = "Stocks")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Stock {

    /**
     * Уникальный идентификатор акции.
     * Автоматически генерируется при сохранении в базу данных.
     */
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Биржевой тикер акции.
     * Не может быть null. Максимальная длина — 5 символов.
     */
    @Expose
    @Column(name = "ticket", nullable = false, length = 5)
    private String ticket;

    /**
     * Текущая цена акции.
     * Не может быть null.
     */
    @Expose
    @Column(name = "price", nullable = false)
    private Double price;

    /**
     * Количество акций в наличии.
     * Не может быть null.
     */
    @Expose
    @Column(name = "amount", nullable = false)
    private Integer amount;

    /**
     * Переопределённый метод сравнения объектов на равенство.
     * Учитывает возможную прокси-обёртку Hibernate для корректного сравнения.
     *
     * @param o объект для сравнения
     * @return true, если объекты равны по типу и идентификатору, иначе false
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
        Stock stock = (Stock) o;
        return getId() != null && Objects.equals(getId(), stock.getId());
    }

    /**
     * Переопределённый метод генерации хеш-кода.
     * Учитывает прокси-обёртку Hibernate для корректности.
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
