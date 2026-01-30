package org.example.shared.model.service;

import jakarta.transaction.Transactional;
import org.example.shared.entityForm.AddressForm;
import org.example.shared.model.entity.Address;
import org.example.shared.model.entity.User;
import org.example.shared.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    public List<Address> findActiveByUser(User user) {
        return addressRepository.findByUserAndIsActiveTrue(user);
    }

    public Address findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adresse introuvable avec l'ID : " + id));
    }

    @Transactional

    public void createAddress(AddressForm form, User user) {

        Address entity = new Address();
        entity.setStreet(form.getStreet());
        entity.setCity(form.getCity());
        entity.setZipCode(form.getZipCode());
        entity.setCountry(form.getCountry());
        entity.setIsActive(true);
        entity.setUser(user);
        addressRepository.save(entity);
    }

    @Transactional
    public void softDelete(Long addressId, String userEmail) {

        Address address = findById(addressId);
        validateOwnership(address, userEmail);

        address.setIsActive(false);
        addressRepository.save(address);
    }

    @Transactional
    public void updateAddress(Address updatedData, String userEmail) {

        Address existing = findById(updatedData.getId());
        validateOwnership(existing, userEmail);


        existing.setStreet(updatedData.getStreet());
        existing.setCity(updatedData.getCity());
        existing.setZipCode(updatedData.getZipCode());
        existing.setCountry(updatedData.getCountry());
        existing.setIsActive(true);

        addressRepository.save(existing);
    }

    private void validateOwnership(Address address, String email) {
        if (!address.getUser().getEmail().equals(email)) {
            throw new SecurityException("Accès non autorisé à cette adresse");
        }
    }
}
