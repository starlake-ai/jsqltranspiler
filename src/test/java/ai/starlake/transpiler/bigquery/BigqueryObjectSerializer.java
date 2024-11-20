package ai.starlake.transpiler.bigquery;

import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.FieldValueList;

import java.util.stream.Collectors;

public class BigqueryObjectSerializer {
    public static Object serialize(Object object){
        if(object == null){
            return null;
        } else if(object instanceof FieldValueList){
            FieldValueList list = (FieldValueList) object;
            return list.stream().map(BigqueryObjectSerializer::serialize).collect(Collectors.toList());
        } else if(object instanceof FieldValue){
            FieldValue fv = (FieldValue) object;
            if(fv.getValue() instanceof FieldValueList){
                return BigqueryObjectSerializer.serialize(fv.getValue());
            } else {
                return String.valueOf(fv.getValue());
            }
        } else {
            return String.valueOf(object);
        }
    }
}
