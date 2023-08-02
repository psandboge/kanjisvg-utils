package se.sandboge.kanjisvg;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class SvgBean {
    private SvgElement svg;

    @Override
    public String toString() {
        return "SvgBean{" +
                "svg=" + svg +
                '}';
    }

    public SvgElement getSvg() {
        return svg;
    }

    public void setSvg(SvgElement svg) {
        this.svg = svg;
    }
}
