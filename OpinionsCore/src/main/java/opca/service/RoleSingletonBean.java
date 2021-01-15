package opca.service;

import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import opca.model.Role;
import opca.model.User;
import opca.repository.RoleRepository;
import opca.repository.UserRepository;

/**
 * This class is a singleton that loads and holds all Role definitions from 
 * the database. 
 * 
 * @author Karl Nicholas
 *
 */
//@Singleton
@Component
public class RoleSingletonBean {
    private List<Role> allRoles;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleSingletonBean(RoleRepository roleRepository, UserRepository userRepository) {
		super();
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
	}

	//private constructor to avoid client applications to use constructor
    @PostConstruct
    protected void postConstruct(){
        allRoles = roleRepository.listAvailable();
        // initialize if needed
        if ( allRoles.size() == 0 ) {
        	Role userRole = new Role();
        	userRole.setRole("USER");
        	roleRepository.save(userRole);
        	allRoles.add(userRole);
        	Role adminRole = new Role();
        	adminRole.setRole("ADMIN");
        	roleRepository.save(adminRole);
        	allRoles.add(adminRole);
        	// might as well add an administrator now as well.
        	User admin = new User("karl.nicholas@outlook.com", true, "N3sPSBxOjdhCygeA8LkqtBskJ+v8TR0do4zJRTIQ4Aw=", Locale.US);
        	admin.setFirstName("Karl");
        	admin.setLastName("Nicholas");
        	admin.setVerified(true);
        	admin.setRoles(allRoles);
        	userRepository.save(admin);
        }
    }

    /**
     * Get the USER Role
     * @return USER Role
     */
    public Role getUserRole()  {
        for ( Role role: allRoles ) {
            if ( role.getRole().equals("USER")) return role;
        }
        throw new RuntimeException("Role USER not found"); 
    }
    /**
     * Get the ADMIN Role
     * @return ADMIN Role
     */
    public Role getAdminRole()  {
        for ( Role role: allRoles ) {
            if ( role.getRole().equals("ADMIN")) return role;
        }
        throw new RuntimeException("Role ADMIN not found"); 
    }
}