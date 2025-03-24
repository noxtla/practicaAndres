// Paquete donde se encuentra la clase
package com.andres.curso.springboot.app.springbootcrud.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andres.curso.springboot.app.springbootcrud.entities.Product;
import com.andres.curso.springboot.app.springbootcrud.repositories.ProductRepository;

// Anotación que indica que esta clase es un componente de servicio de Spring
@Service
public class ProductServiceImpl implements ProductService {

    // Inyección automática del repositorio de productos
    @Autowired
    private ProductRepository repository;

    // Método para obtener todos los productos. Transacción solo de lectura.
    @Transactional(readOnly = true)
    @Override
    public List<Product> findAll() {
        return (List<Product>) repository.findAll();
    }

    // Método para obtener un producto por su ID. Transacción solo de lectura.
    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    // Método para guardar un nuevo producto o actualizar uno existente. Transacción de escritura.
    @Override
    @Transactional
    public Product save(Product product) {
        return repository.save(product);
    }

    // Método para actualizar un producto existente por su ID.
    @Override
    @Transactional
    public Optional<Product> update(Long id, Product product) {
        // Se busca el producto por ID
        Optional<Product> productOptional = repository.findById(id);
        if (productOptional.isPresent()) {
            // Si se encuentra, se actualizan sus atributos con los nuevos datos
            Product productDb = productOptional.orElseThrow();

            productDb.setSku(product.getSku());
            productDb.setName(product.getName());
            productDb.setDescription(product.getDescription());
            productDb.setPrice(product.getPrice());

            // Se guarda y retorna el producto actualizado
            return Optional.of(repository.save(productDb));
        }
        // Si no se encuentra el producto, se retorna el Optional vacío
        return productOptional;
    }

    // Método para eliminar un producto por ID
    @Transactional
    @Override
    public Optional<Product> delete(Long id) {
        // Se busca el producto
        Optional<Product> productOptional = repository.findById(id);
        // Si existe, se elimina
        productOptional.ifPresent(productDb -> {
            repository.delete(productDb);
        });
        return productOptional;
    }

    // Método para verificar si ya existe un producto con un SKU determinado
    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return repository.existsBySku(sku);
    }
}
