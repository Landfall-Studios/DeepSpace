package world.landfall.deepspace.integration;


public class DeepspaceOptions {

    public enum Detail {
        NONE("options.deepspace.decorationDetail.none"), BASIC("options.deepspace.decorationDetail.basic"), EXPENSIVE("options.deepspace.decorationDetail.expensive");
        final String value;
        Detail(String _value) {
            value = _value;
        }
        @Override
        public String toString() {
            return value;
        }
    }
    public Detail atmosphereDetail;
    public Detail shadingDetail;

    public DeepspaceOptions(Detail atmosphereDetail, Detail shadingDetail) {

        this.atmosphereDetail = atmosphereDetail;
        this.shadingDetail = shadingDetail;
    }
    public void load(Detail atmosphereDetail, Detail shadingDetail) {
        this.atmosphereDetail = atmosphereDetail;
        this.shadingDetail = shadingDetail;
    }
    public static DeepspaceOptions defaults() {
        return new DeepspaceOptions(Detail.BASIC, Detail.BASIC);
    }

}
