package model;

import com.opencsv.bean.CsvBindByPosition;

import java.util.Objects;

/**
 * Модель сущности
 */
public class Product implements Comparable<Product> {

    @CsvBindByPosition(position = 0)
    int id;

    @CsvBindByPosition(position = 1)
    String name;

    @CsvBindByPosition(position = 2)
    String condition;

    @CsvBindByPosition(position = 3)
    String state;

    @CsvBindByPosition(position = 4)
    float price;

    //по документации нужен для openCSV
    public Product() {
    }


    public int getId() {
        return id;
    }

    public float getPrice() {
        return price;
    }


    @Override
    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o == null || getClass ( ) != o.getClass ( ) ) return false;

        Product product = (Product) o;

        if ( id != product.id ) return false;
        if ( Float.compare (product.price, price) != 0 ) return false;
        if (!Objects.equals(name, product.name)) return false;
        if (!Objects.equals(condition, product.condition)) return false;
        return Objects.equals(state, product.state);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode ( ) : 0);
        result = 31 * result + (condition != null ? condition.hashCode ( ) : 0);
        result = 31 * result + (state != null ? state.hashCode ( ) : 0);
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits (price) : 0);
        return result;
    }


    @Override
    public int compareTo(Product o) {
        return Float.compare (this.price,o.price);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", condition='" + condition + '\'' +
                ", state='" + state + '\'' +
                ", price=" + price +
                '}';
    }
}
