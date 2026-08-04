package com.ecommerce.inventory_service.service.impl;

import com.ecommerce.inventory_service.dto.InventoryRequest;
import com.ecommerce.inventory_service.dto.InventoryResponse;
import com.ecommerce.inventory_service.exception.ResourceNotFoundException;
import com.ecommerce.inventory_service.mapper.InventoryMapper;
import com.ecommerce.inventory_service.model.Inventory;
import com.ecommerce.inventory_service.repository.InventoryRepository;
import com.ecommerce.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Value("${inventory.allow-backorders: false}")
    private boolean allowBackorders;

    @Override
    @Transactional(readOnly = true)
    public boolean isInStock(String sku, Integer quantity) {
        if(allowBackorders){
            log.warn("MODO BACKORDER ACTIVO: Autorizando stock para SKU: {}",sku);
            return true;
        }
        return inventoryRepository.findBySku(sku).map(inventory -> inventory.getQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryRequest inventoryRequest) {
        boolean exists = inventoryRepository.existsBySku(inventoryRequest.getSku());
        if(exists){
            throw new RuntimeException("El inventario para el sku: "+inventoryRequest.getSku()+" ya existe");
        }
        Inventory inventory = inventoryMapper.toModel(inventoryRequest);
        Inventory saved = inventoryRepository.save(inventory);
        log.info("Inventario creado para el sku: {}", saved.getSku());
        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream().map(inventoryMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long id, InventoryRequest inventoryRequest) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Inventario","id",id)
        );
        inventory.setSku(inventoryRequest.getSku());
        inventory.setQuantity(inventoryRequest.getQuantity());
        Inventory update = inventoryRepository.save(inventory);
        log.info("Inventario actualizado para el id: {}",id);
        return inventoryMapper.toResponse(update);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        if(!inventoryRepository.existsById(id)){
            throw new ResourceNotFoundException("Inventario","id",id);
        }
        inventoryRepository.deleteById(id);
        log.info("Inventario eliminado con id: {}",id);
    }

    @Override
    @Transactional
    public void reduceStock(String sku, Integer quantity) {
        var inventory = inventoryRepository.findBySku(sku).orElseThrow(
                () -> new RuntimeException("Producto no encontrado: "+sku)
        );
        if(inventory.getQuantity() < quantity){
            throw new RuntimeException("Stock insuficiente para: "+sku);
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }
}
