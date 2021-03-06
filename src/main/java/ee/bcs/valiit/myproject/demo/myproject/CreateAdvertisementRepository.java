package ee.bcs.valiit.myproject.demo.myproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CreateAdvertisementRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void createAdvertisement(AdvertisementDTO advertisementDTO) {
        String sql = "INSERT INTO advertisement (title, description, price, category, location, username, phonenumber, email, time)" +
                " VALUES (:dbTitle, :dbDescription, :dbPrice, :dbCategory, :dbLocation, :dbUsername, :dbPhoneNumber, :dbEmail, :dbTime)";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dbTitle", advertisementDTO.getTitle());
        paramMap.put("dbDescription", advertisementDTO.getDescription());
        paramMap.put("dbPrice", advertisementDTO.getPrice());
        paramMap.put("dbCategory", advertisementDTO.getCategory());
        paramMap.put("dbLocation", advertisementDTO.getLocation());
        paramMap.put("dbUsername", advertisementDTO.getUsername());
        paramMap.put("dbPhoneNumber", advertisementDTO.getPhonenumber());
        paramMap.put("dbEmail", advertisementDTO.getEmail());
        paramMap.put("dbTime", LocalDateTime.now());
        jdbcTemplate.update(sql, paramMap);
    }
    public void savePhoto(MultipartFile file) throws IOException {
        String sql= "INSERT INTO photodatabase (photo) values (:dbPhoto)";
        MapSqlParameterSource paramMap = new MapSqlParameterSource();
        paramMap.addValue("dbPhoto",
                new SqlLobValue(new ByteArrayInputStream(file.getBytes()), file.getBytes().length, new DefaultLobHandler()));
        jdbcTemplate.update(sql, paramMap);
    }

    public List<AdvertisementDTO> getAllAdvertisements() {
        String sql = "SELECT * FROM advertisement";
        return jdbcTemplate.query(sql, new HashMap(), new AdvertisementRowMapper());
    }

    public AdvertisementDTO getAdvertisement(int id) {
        String sql = "SELECT * FROM advertisement a FULL OUTER JOIN photodatabase p  ON a.id=p.id WHERE a.id=:dbId";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dbId", id);
        return jdbcTemplate.queryForObject(sql, paramMap, new AdvertisementRowMapper());
    }

    public List<AdvertisementDTO> getAdsByPrice(Double priceFrom, Double priceTo) {
        String sql = "SELECT * FROM advertisement WHERE true ";
        Map<String, Object> paramMap = new HashMap<>();
        if (priceFrom != null) {
            sql += "AND :priceFrom <= price ";
            paramMap.put("priceFrom", priceFrom);
        }
        if (priceTo != null) {
            sql += "AND price <= :priceTo ";
            paramMap.put("priceTo", priceTo);
        }
        return jdbcTemplate.query(sql, paramMap, new AdvertisementRowMapper());
    }

    public List<AdvertisementDTO> getAdsByCategory(String category) {
        String sql = "SELECT * FROM advertisement WHERE category=:dbCategory";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dbCategory", category);
        return jdbcTemplate.query(sql, paramMap, new AdvertisementRowMapper());
    }

    public List<AdvertisementDTO> getAdsByLocation(String location) {
        String sql = "SELECT * FROM advertisement WHERE location=:dbLocation";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dbLocation", location);
        return jdbcTemplate.query(sql, paramMap, new AdvertisementRowMapper());
    }

    public List<AdvertisementDTO> filterAdsByPriceCategoryLocation(String category, String location, Double priceFrom, Double priceTo, String input, String orderByColumn, String orderByDirection) {
        String sql = "SELECT * FROM advertisement a FULL OUTER JOIN photodatabase p  ON a.id=p.id WHERE true ";
        Map<String, Object> paramMap = new HashMap<>();
        if (category != null && !category.isBlank()) {
            sql += "AND :dbCategory = category ";
            paramMap.put("dbCategory", category);
        }
        if (location != null && !location.isBlank()) {
            sql += "AND :dbLocation = location ";
            paramMap.put("dbLocation", location);
        }
        if (priceFrom != null) {
            sql += "AND :priceFrom <= price ";
            paramMap.put("priceFrom", priceFrom);
        }
        if (priceTo != null) {
            sql += "AND price <= :priceTo ";
            paramMap.put("priceTo", priceTo);
        }
        if (input != null && !input.isBlank()) {
            sql += "AND title ilike :dbTitle OR description ilike :dbTitle ";
            paramMap.put("dbTitle", "%" + input + "%");
        }
        if (orderByColumn != null && orderByDirection != null
                && !orderByColumn.isBlank() && !orderByDirection.isBlank()) {
            validateOrderBy(orderByColumn, orderByDirection);
            sql += "ORDER BY " + orderByColumn + " " + orderByDirection;
        }
        return jdbcTemplate.query(sql, paramMap, new AdvertisementRowMapper());
    }

    private void validateOrderBy(String orderByColumn, String orderByDirection) {
        if(!orderByDirection.equalsIgnoreCase("ASC") && !orderByDirection.equalsIgnoreCase("DESC")){
            throw new RuntimeException("Viga");
        }
        if(!orderByColumn.equalsIgnoreCase("price")  && !orderByColumn.equalsIgnoreCase("time")){
            throw new RuntimeException("Viga");
        }
    }


    public List <AdvertisementDTO> searchAdsByTitleDescription(String input){
        String sql = "SELECT * FROM advertisement a FULL OUTER JOIN photodatabase p  ON a.id=p.id WHERE title ilike :dbTitle OR description ilike :dbTitle OR category ilike :dbTitle OR location ilike :dbTitle OR username ilike :dbTitle";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dbTitle", "%" + input + "%");
        return jdbcTemplate.query(sql, paramMap, new AdvertisementRowMapper());
    }
}

