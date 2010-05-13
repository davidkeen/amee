package com.amee.platform.service.v3.search;

import com.amee.base.resource.RequestWrapper;
import com.amee.platform.search.SearchService;
import com.amee.platform.service.v3.category.DataCategoryBuilder;
import com.amee.platform.service.v3.category.DataCategoryDOMBuilder;
import com.amee.platform.service.v3.item.DataItemBuilder;
import com.amee.platform.service.v3.item.DataItemDOMBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SearchDOMBuilder extends SearchBuilder<Document> {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataCategoryDOMBuilder dataCategoryDOMBuilder;

    @Autowired
    private DataItemDOMBuilder dataItemDOMBuilder;

    @Autowired
    private SearchFilterValidationHelper validationHelper;

    public Document handle(RequestWrapper requestWrapper) {
        SearchDOMRenderer renderer =
                new SearchDOMRenderer(
                        new DataCategoryDOMBuilder.DataCategoryDOMRenderer(),
                        new DataItemDOMBuilder.DataItemDOMRenderer());
        super.handle(requestWrapper, renderer);
        return renderer.getDocument();
    }

    public String getMediaType() {
        return "application/json";
    }

    public DataCategoryBuilder getDataCategoryBuilder() {
        return dataCategoryDOMBuilder;
    }

    public DataItemBuilder getDataItemBuilder() {
        return dataItemDOMBuilder;
    }

    public class SearchDOMRenderer implements SearchRenderer {

        private DataCategoryDOMBuilder.DataCategoryDOMRenderer dataCategoryRenderer;
        private DataItemDOMBuilder.DataItemDOMRenderer dataItemRenderer;
        private Element rootElem;
        private Element categoriesElem;
        private Element itemsElem;

        public SearchDOMRenderer(
                DataCategoryDOMBuilder.DataCategoryDOMRenderer dataCategoryRenderer,
                DataItemDOMBuilder.DataItemDOMRenderer dataItemRenderer) {
            super();
            this.dataCategoryRenderer = dataCategoryRenderer;
            this.dataItemRenderer = dataItemRenderer;
        }

        public void start() {
            rootElem = new Element("Representation");
        }

        public void ok() {
            rootElem.addContent(new Element("Status").setText("OK"));
        }

        public void notAuthenticated() {
            rootElem.addContent(new Element("Status").setText("NOT_AUTHENTICATED"));
        }

        public void newDataCategory() {
            if (categoriesElem == null) {
                categoriesElem = new Element("Categories");
                rootElem.addContent(categoriesElem);
            }
            categoriesElem.addContent(dataCategoryRenderer.getDataCategoryElement());
        }

        public void newDataItem() {
            if (itemsElem == null) {
                itemsElem = new Element("Items");
                rootElem.addContent(itemsElem);
            }
            itemsElem.addContent(dataItemRenderer.getDataItemElement());
        }

        public DataCategoryBuilder.DataCategoryRenderer getDataCategoryRenderer() {
            return dataCategoryRenderer;
        }

        public DataItemBuilder.DataItemRenderer getDataItemRenderer() {
            return dataItemRenderer;
        }

        public Document getDocument() {
            return new Document(rootElem);
        }
    }
}