package MAMS.Backend;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@SpringBootApplication
public class BackendApplication {
    private static final String SECRET = "MAMS-production-demo-secret-key-change-in-render-env-2026";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static void main(String[] args) { SpringApplication.run(BackendApplication.class, args); }

    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.csrf(c -> c.disable()).cors(c -> {}).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.requestMatchers("/api/auth/login", "/error").permitAll().anyRequest().authenticated())
            .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class).build();
    }

    static String token(String username) {
        return Jwts.builder().subject(username).claim("role", "ADMIN").issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(86400))).signWith(KEY).compact();
    }

    static class JwtFilter extends org.springframework.web.filter.OncePerRequestFilter {
        protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res, jakarta.servlet.FilterChain chain) throws java.io.IOException, jakarta.servlet.ServletException {
            String h=req.getHeader("Authorization");
            if(h!=null && h.startsWith("Bearer ")) try {
                var c=Jwts.parser().verifyWith(KEY).build().parseSignedClaims(h.substring(7)).getPayload();
                var auth=new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(c.getSubject(),null,List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            } catch(Exception ignored) {}
            chain.doFilter(req,res);
        }
    }

    @Bean org.springframework.boot.CommandLineRunner init(JdbcTemplate db) { return args -> {
        db.execute("CREATE TABLE IF NOT EXISTS bases(id BIGSERIAL PRIMARY KEY,name VARCHAR(120) UNIQUE NOT NULL,location VARCHAR(120) NOT NULL)");
        db.execute("CREATE TABLE IF NOT EXISTS equipment(id BIGSERIAL PRIMARY KEY,name VARCHAR(160) NOT NULL,category VARCHAR(120),quantity INT NOT NULL DEFAULT 0,base_id BIGINT REFERENCES bases(id))");
        db.execute("CREATE TABLE IF NOT EXISTS purchases(id BIGSERIAL PRIMARY KEY,base_id BIGINT REFERENCES bases(id),equipment_type VARCHAR(120),quantity INT NOT NULL,purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,supplier VARCHAR(160))");
        db.execute("CREATE TABLE IF NOT EXISTS transfers(id BIGSERIAL PRIMARY KEY,from_base BIGINT REFERENCES bases(id),to_base BIGINT REFERENCES bases(id),equipment_type VARCHAR(120),quantity INT NOT NULL,transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        db.execute("CREATE TABLE IF NOT EXISTS assignments(id BIGSERIAL PRIMARY KEY,base_id BIGINT REFERENCES bases(id),equipment_type VARCHAR(120),quantity INT NOT NULL,personnel VARCHAR(160),assignment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        db.execute("CREATE TABLE IF NOT EXISTS expenditures(id BIGSERIAL PRIMARY KEY,base_id BIGINT REFERENCES bases(id),equipment_type VARCHAR(120),quantity INT NOT NULL,reason VARCHAR(255),expenditure_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        db.execute("CREATE TABLE IF NOT EXISTS audit_logs(id BIGSERIAL PRIMARY KEY,username VARCHAR(120),action VARCHAR(120),entity_type VARCHAR(120),entity_id BIGINT,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        if(db.queryForObject("SELECT COUNT(*) FROM bases",Long.class)==0) db.update("INSERT INTO bases(name,location) VALUES('Bangalore Base','Bangalore')");
    }; }

    @RestController @RequestMapping("/api/auth") static class Auth {
        @PostMapping("/login") Map<String,String> login(@RequestBody Map<String,String> b){
            if("admin".equals(b.get("username")) && "Admin@123".equals(b.get("password"))) return Map.of("token",token("admin"));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
        }
    }

    @RestController @RequestMapping("/api") static class Api {
        final JdbcTemplate db; Api(JdbcTemplate db){this.db=db;}
        private List<Map<String,Object>> q(String sql,Object... p){return db.queryForList(sql,p);}
        private void audit(String action,String entity,Long id){db.update("INSERT INTO audit_logs(username,action,entity_type,entity_id) VALUES(?,?,?,?)", "admin",action,entity,id);}

        @GetMapping("/dashboard") Map<String,Object> dashboard(){
            long purchases=n("SELECT COALESCE(SUM(quantity),0) FROM purchases"), inT=n("SELECT COALESCE(SUM(quantity),0) FROM transfers"), assigned=n("SELECT COALESCE(SUM(quantity),0) FROM assignments"), exp=n("SELECT COALESCE(SUM(quantity),0) FROM expenditures");
            long outT=0; long opening=0; long closing=opening+purchases+inT-outT-assigned-exp;
            return Map.of("openingBalance",opening,"purchases",purchases,"transferIn",inT,"transferOut",outT,"assigned",assigned,"expended",exp,"netMovement",purchases+inT-outT,"closingBalance",closing);
        }
        long n(String sql){return db.queryForObject(sql,Long.class);}
        @GetMapping("/bases") List<Map<String,Object>> bases(){return q("SELECT id,name,location FROM bases ORDER BY id");}
        @PostMapping("/bases") Map<String,Object> addBase(@RequestBody Map<String,Object> b){db.update("INSERT INTO bases(name,location) VALUES(?,?)",b.get("name"),b.get("location"));audit("CREATE","BASE",1L);return Map.of("message","Base created");}
        @GetMapping("/equipment") List<Map<String,Object>> equipment(){return q("SELECT e.id,e.name,e.category,e.quantity,e.base_id,b.name base_name FROM equipment e LEFT JOIN bases b ON b.id=e.base_id ORDER BY e.id");}
        @PostMapping("/equipment") Map<String,Object> addEquipment(@RequestBody Map<String,Object> b){db.update("INSERT INTO equipment(name,category,quantity,base_id) VALUES(?,?,?,?)",b.get("name"),b.get("category"),b.get("quantity"),b.get("baseId"));audit("CREATE","EQUIPMENT",1L);return Map.of("message","Equipment created");}
        @GetMapping("/purchases") List<Map<String,Object>> purchases(){return q("SELECT * FROM purchases ORDER BY id DESC");}
        @PostMapping("/purchases") Map<String,Object> purchase(@RequestBody Map<String,Object> b){db.update("INSERT INTO purchases(base_id,equipment_type,quantity,supplier) VALUES(?,?,?,?)",b.get("baseId"),b.get("equipmentType"),b.get("quantity"),b.get("supplier"));audit("CREATE","PURCHASE",1L);return Map.of("message","Purchase recorded");}
        @GetMapping("/transfers") List<Map<String,Object>> transfers(){return q("SELECT * FROM transfers ORDER BY id DESC");}
        @PostMapping("/transfers") Map<String,Object> transfer(@RequestBody Map<String,Object> b){db.update("INSERT INTO transfers(from_base,to_base,equipment_type,quantity) VALUES(?,?,?,?)",b.get("fromBase"),b.get("toBase"),b.get("equipmentType"),b.get("quantity"));audit("CREATE","TRANSFER",1L);return Map.of("message","Transfer recorded");}
        @GetMapping("/assignments") List<Map<String,Object>> assignments(){return q("SELECT * FROM assignments ORDER BY id DESC");}
        @GetMapping("/expenditures") List<Map<String,Object>> expenditures(){return q("SELECT * FROM expenditures ORDER BY id DESC");}
        @GetMapping("/audit-logs") List<Map<String,Object>> logs(){return q("SELECT * FROM audit_logs ORDER BY id DESC");}
    }
}
