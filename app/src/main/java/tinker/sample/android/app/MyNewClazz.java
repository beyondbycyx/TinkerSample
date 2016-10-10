package tinker.sample.android.app;

/**
 * Created by huangquan on 16/10/10.
 */

public class MyNewClazz {

    public String name;
    public int id ;

    public MyNewClazz(String name, int id) {
        this.name = name ;
        this.id = id;

    }


    @Override
    public String toString() {
        return "MyNewClazz{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
