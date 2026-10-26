package com.retailos.backend;

import java.util.List;

import org.springframework.stereotype.Service;

@Service 
public class InventoryCsvService {
    
    private final ProductService service;

    InventoryCsvService(ProductService service){
        this.service = service;
    }

    private String escapeCell(String value){
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    public String exportProducts(String category){
        StringBuilder csv = new StringBuilder();
        csv.append("id,name,quantity,category,expirationDate\r\n");
        List<Product> products;
        if(category == null){
            products = service.getProducts();
        }else{
            products = service.findProductsByCategory(category);
        }
        for(Product product : products){
            csv.append(escapeCell(String.valueOf(product.id())));
            csv.append(",");
            csv.append(escapeCell(product.name()));
            csv.append(",");
            csv.append(escapeCell(String.valueOf(product.quantity())));
            csv.append(",");
            csv.append(escapeCell(product.category()));
            csv.append(",");
            String expirationDate;
            if(product.expirationDate() == null){
                expirationDate = "";
            }else{
                expirationDate = product.expirationDate().toString();
            }
            csv.append(escapeCell(expirationDate));
            csv.append("\r\n");
        }
        return csv.toString();

    }
}
