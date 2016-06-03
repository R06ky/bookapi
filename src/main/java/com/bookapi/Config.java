package bookapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by rockyren on 5/1/16.
 */
@Component
public class Config {

    @Value("${config.jdimgprefixfrom}")
    String jdimgprefixfrom;
    @Value("${config.jdimgprefixto}")
    String jdimgprefixto;
    @Value("${config.jddestimeout}")
    int jddestimeout;
    @Value("${config.jdemptydesc}")
    String jdemptydesc;

    public String getJdemptydesc() {
        return jdemptydesc;
    }

    public void setJdemptydesc(String jdemptydesc) {
        this.jdemptydesc = jdemptydesc;
    }

    public String getJdimgprefixfrom() {
        return jdimgprefixfrom;
    }

    public void setJdimgprefixfrom(String jdimgprefixfrom) {
        this.jdimgprefixfrom = jdimgprefixfrom;
    }

    public String getJdimgprefixto() {
        return jdimgprefixto;
    }

    public void setJdimgprefixto(String jdimgprefixto) {
        this.jdimgprefixto = jdimgprefixto;
    }

    public int getJddestimeout() {
        return jddestimeout;
    }

    public void setJddestimeout(int jddestimeout) {
        this.jddestimeout = jddestimeout;
    }
}
