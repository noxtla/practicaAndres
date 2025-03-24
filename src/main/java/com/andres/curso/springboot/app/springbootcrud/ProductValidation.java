// Paquete donde se encuentra la clase
package com.andres.curso.springboot.app.springbootcrud;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.andres.curso.springboot.app.springbootcrud.entities.Product;

// Marca esta clase como un componente de Spring para que pueda ser inyectada automáticamente donde se necesite
@Component
public class ProductValidation implements Validator {

    // Este método define qué tipo de clases puede validar este validador.
    // Aquí se indica que puede validar objetos de tipo Product (o subtipos)
    @Override
    public boolean supports(Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }

    // Método principal de validación. Aquí se define la lógica para validar un objeto Product.
    @Override
    public void validate(Object target, Errors errors) {
        // Se convierte el objeto recibido al tipo Product
        Product product = (Product) target;

        // Verifica si el campo "name" está vacío o contiene solo espacios en blanco
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", null, "es requerido!");

        // Valida que la descripción no sea nula ni vacía
        if (product.getDescription() == null || product.getDescription().isBlank()) {
            errors.rejectValue("description", null, "es requerido, por favor");
        }

        // Valida que el precio no sea nulo y que sea mayor o igual a 500
        if (product.getPrice() == null) {
            errors.rejectValue("price", null, "no puede ser nulo, ok!");
        } else if (product.getPrice() < 500) {
            errors.rejectValue("price", null, "debe ser un valor numerico mayor o igual que 500!");
        }
    }
}
