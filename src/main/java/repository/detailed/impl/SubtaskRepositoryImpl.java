package repository.detailed.impl;

import objects.Subtask;
import objects.Variant;
import org.w3c.dom.Element;
import repository.XMLStorageConnector;
import repository.detailed.SubtaskRepository;
import repository.impl.ParentRepositoryImpl;

import java.util.Arrays;
import java.util.List;

public class SubtaskRepositoryImpl
        extends ParentRepositoryImpl<Subtask, Variant>
        implements SubtaskRepository {

    public SubtaskRepositoryImpl(XMLStorageConnector xmlStorageConnector, VariantRepositoryImpl variantRepository){
        super(xmlStorageConnector, variantRepository);
    }

    @Override
    protected String getElementTagName(){
        return ELEMENT_TAG_NAME;
    }

    @Override
    protected String getChildTagName(){
        return CHILD_TAG_NAME;
    }


    @Override
    protected void mapElementFields(Element element, Subtask target){
        super.mapElementFields(element, target);
        mapSubtaskAttributes(element, target);
    }

    private void mapSubtaskAttributes(Element element, Subtask target){
        target.setPoints(getIntAttribute(element, POINT_ATTRIBUTE_LABEL));
        target.setChapterId(getIntAttribute(element, PARENT_ID_ATTRIBUTE_LABEL));
        target.setLabels(parseLabels(getStringAttribute(element, LABELS_ATTRIBUTE_LABEL)));
    }

    private List<String> parseLabels(String rawLabels) {
        if (rawLabels == null || rawLabels.isBlank()) {
            return List.of();
        }
        return Arrays.stream(rawLabels.split(","))
                .map(String::trim)
                .filter(label -> !label.isBlank())
                .toList();
    }
}
