package com.trabajoFinal.bookReviews.service;

import com.trabajoFinal.bookReviews.entity.Usuario;
import com.trabajoFinal.bookReviews.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // 1. Buscamos el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + usernameOrEmail));

        // 2. Convertimos la LISTA de roles (Strings) a Permisos de Seguridad (GrantedAuthority)
        List<GrantedAuthority> autoridades = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol))
                .collect(Collectors.toList());

        // 3. Devolvemos el objeto User oficial de Spring Security
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                autoridades
        );
    }
}