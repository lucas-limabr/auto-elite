package com.concessionaria.config;

import com.concessionaria.entity.*;
import com.concessionaria.enums.FuelType;
import com.concessionaria.enums.Role;
import com.concessionaria.enums.TransmissionType;
import com.concessionaria.enums.VehicleStatus;
import com.concessionaria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DealershipBranchRepository branchRepository;
    private final OperatorProfileRepository operatorProfileRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final VehicleFeatureRepository featureRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@concessionaria.com")) {
            log.info("DataSeeder: Banco de dados já inicializado.");
            return;
        }

        log.info("DataSeeder: Populando dados iniciais da Concessionária...");

        // 1. Criar Administrador
        User admin = User.builder()
                .name("Administrador do Sistema")
                .email("admin@concessionaria.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);

        // 2. Criar 2 Filiais
        DealershipBranch branchSP = DealershipBranch.builder()
                .name("Concessionária Matriz São Paulo")
                .cnpj("11.222.333/0001-44")
                .city("São Paulo")
                .state("SP")
                .address("Av. das Nações Unidas, 14401 - Chácara Santo Antônio")
                .maxDailyTestDrives(15)
                .build();
        branchRepository.save(branchSP);

        DealershipBranch branchPR = DealershipBranch.builder()
                .name("Concessionária Filial Curitiba")
                .cnpj("22.333.444/0001-55")
                .city("Curitiba")
                .state("PR")
                .address("Av. Sete de Setembro, 5500 - Batel")
                .maxDailyTestDrives(10)
                .build();
        branchRepository.save(branchPR);

        // 3. Criar 2 Operadores
        User op1User = User.builder()
                .name("Carlos Mendes (Operador SP)")
                .email("operador1@concessionaria.com")
                .password(passwordEncoder.encode("operador123"))
                .role(Role.OPERATOR)
                .active(true)
                .build();
        userRepository.save(op1User);

        OperatorProfile op1Profile = OperatorProfile.builder()
                .user(op1User)
                .registrationCode("OP-SP-001")
                .hireDate(LocalDate.of(2022, 3, 15))
                .department("Vendas e Test-Drive")
                .dealershipBranch(branchSP)
                .build();
        operatorProfileRepository.save(op1Profile);

        User op2User = User.builder()
                .name("Mariana Souza (Operador PR)")
                .email("operador2@concessionaria.com")
                .password(passwordEncoder.encode("operador123"))
                .role(Role.OPERATOR)
                .active(true)
                .build();
        userRepository.save(op2User);

        OperatorProfile op2Profile = OperatorProfile.builder()
                .user(op2User)
                .registrationCode("OP-PR-002")
                .hireDate(LocalDate.of(2023, 7, 1))
                .department("Atendimento e Test-Drive")
                .dealershipBranch(branchPR)
                .build();
        operatorProfileRepository.save(op2Profile);

        // 4. Marcas
        Brand bmw = brandRepository.save(Brand.builder().name("BMW").countryOfOrigin("Alemanha").active(true).build());
        Brand toyota = brandRepository.save(Brand.builder().name("Toyota").countryOfOrigin("Japão").active(true).build());
        Brand byd = brandRepository.save(Brand.builder().name("BYD").countryOfOrigin("China").active(true).build());
        Brand porsche = brandRepository.save(Brand.builder().name("Porsche").countryOfOrigin("Alemanha").active(true).build());
        Brand volkswagen = brandRepository.save(Brand.builder().name("Volkswagen").countryOfOrigin("Alemanha").active(true).build());
        Brand volvo = brandRepository.save(Brand.builder().name("Volvo").countryOfOrigin("Suécia").active(true).build());
        Brand ford = brandRepository.save(Brand.builder().name("Ford").countryOfOrigin("Estados Unidos").active(true).build());

        // 5. Categorias
        Category suv = categoryRepository.save(Category.builder().name("SUV").description("Utilitários esportivos espaçosos e versáteis").build());
        Category sedan = categoryRepository.save(Category.builder().name("Sedan").description("Carros sofisticados com amplo espaço e porta-malas").build());
        Category hatch = categoryRepository.save(Category.builder().name("Hatch").description("Modelos ágeis e econômicos para o tráfego urbano").build());
        Category picape = categoryRepository.save(Category.builder().name("Picape").description("Veículos robustos com caçamba para qualquer terreno").build());
        Category eletrico = categoryRepository.save(Category.builder().name("Elétrico").description("Veículos 100% elétricos com alta eficiência energética").build());

        // 6. Opcionais (Features)
        VehicleFeature f1 = featureRepository.save(VehicleFeature.builder().name("Teto Solar Panorâmico").description("Teto de vidro elétrico panorâmico").build());
        VehicleFeature f2 = featureRepository.save(VehicleFeature.builder().name("Bancos em Couro com Aquecimento").description("Assentos revestidos em couro com ajuste elétrico e memória").build());
        VehicleFeature f3 = featureRepository.save(VehicleFeature.builder().name("Piloto Automático Adaptativo (ACC)").description("Controle de cruzeiro adaptativo com frenagem automática").build());
        VehicleFeature f4 = featureRepository.save(VehicleFeature.builder().name("Câmera 360 Graus").description("Visão panorâmica em 360° para manobras de estacionamento").build());
        VehicleFeature f5 = featureRepository.save(VehicleFeature.builder().name("Faróis Full LED Matriciais").description("Faróis inteligentes com ajuste automático de facho").build());
        VehicleFeature f6 = featureRepository.save(VehicleFeature.builder().name("Central Multimídia 12.3\"").description("Central com suporte sem fio a Apple CarPlay e Android Auto").build());
        VehicleFeature f7 = featureRepository.save(VehicleFeature.builder().name("Tração Integral 4x4 / AWD").description("Sistema de tração integral permanente sob demanda").build());

        // 7. 8 Veículos Iniciais
        // Veículo 1: BMW 320i M Sport
        Vehicle v1 = Vehicle.builder()
                .model("BMW 320i M Sport")
                .yearManufacture(2024)
                .yearModel(2024)
                .price(new BigDecimal("345000.00"))
                .mileage(12000)
                .color("Branco Polar")
                .fuelType(FuelType.FLEX)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("9BW320IMSPORT20241")
                .status(VehicleStatus.AVAILABLE)
                .brand(bmw)
                .category(sedan)
                .branch(branchSP)
                .features(List.of(f1, f2, f3, f4, f5, f6))
                .build();
        v1.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=1200&q=80").isPrimary(true).vehicle(v1).build());
        v1.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1200&q=80").isPrimary(false).vehicle(v1).build());
        vehicleRepository.save(v1);

        // Veículo 2: Toyota Hilux SRX Plus
        Vehicle v2 = Vehicle.builder()
                .model("Toyota Hilux SRX Plus 2.8 4x4")
                .yearManufacture(2023)
                .yearModel(2024)
                .price(new BigDecimal("320000.00"))
                .mileage(24000)
                .color("Prata Metálico")
                .fuelType(FuelType.DIESEL)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("8AJHILUXSRXPLUS24")
                .status(VehicleStatus.AVAILABLE)
                .brand(toyota)
                .category(picape)
                .branch(branchPR)
                .features(List.of(f2, f3, f4, f6, f7))
                .build();
        v2.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=1200&q=80").isPrimary(true).vehicle(v2).build());
        vehicleRepository.save(v2);

        // Veículo 3: BYD Song Plus DM-i
        Vehicle v3 = Vehicle.builder()
                .model("BYD Song Plus DM-i 1.5 Plug-in")
                .yearManufacture(2024)
                .yearModel(2025)
                .price(new BigDecimal("239800.00"))
                .mileage(5000)
                .color("Cinza Titânio")
                .fuelType(FuelType.HYBRID)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("LGDSONGPLUS2025DMI")
                .status(VehicleStatus.AVAILABLE)
                .brand(byd)
                .category(suv)
                .branch(branchSP)
                .features(List.of(f1, f2, f3, f4, f5, f6))
                .build();
        v3.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=1200&q=80").isPrimary(true).vehicle(v3).build());
        vehicleRepository.save(v3);

        // Veículo 4: Porsche Taycan 4S
        Vehicle v4 = Vehicle.builder()
                .model("Porsche Taycan 4S Elétrico")
                .yearManufacture(2023)
                .yearModel(2023)
                .price(new BigDecimal("680000.00"))
                .mileage(8500)
                .color("Azul Gentian")
                .fuelType(FuelType.ELECTRIC)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("WP0TAYCAN4S2023EV")
                .status(VehicleStatus.AVAILABLE)
                .brand(porsche)
                .category(eletrico)
                .branch(branchSP)
                .features(List.of(f1, f2, f3, f4, f5, f6, f7))
                .build();
        v4.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1614162692292-7ac56d7f7f1e?auto=format&fit=crop&w=1200&q=80").isPrimary(true).vehicle(v4).build());
        vehicleRepository.save(v4);

        // Veículo 5: Volkswagen Golf GTI MK8
        Vehicle v5 = Vehicle.builder()
                .model("Volkswagen Golf GTI 2.0 TSI")
                .yearManufacture(2022)
                .yearModel(2023)
                .price(new BigDecimal("215000.00"))
                .mileage(32000)
                .color("Vermelho Tornado")
                .fuelType(FuelType.GASOLINE)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("3VWGOLFGTI2023MK8")
                .status(VehicleStatus.AVAILABLE)
                .brand(volkswagen)
                .category(hatch)
                .branch(branchPR)
                .features(List.of(f1, f2, f3, f5, f6))
                .build();
        v5.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=1200&q=80").isPrimary(true).vehicle(v5).build());
        vehicleRepository.save(v5);

        // Veículo 6: Volvo XC60 Recharge T8
        Vehicle v6 = Vehicle.builder()
                .model("Volvo XC60 Recharge T8 AWD")
                .yearManufacture(2024)
                .yearModel(2024)
                .price(new BigDecimal("420000.00"))
                .mileage(11000)
                .color("Preto Onyx")
                .fuelType(FuelType.HYBRID)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("YV4XC60RECHARGE24")
                .status(VehicleStatus.AVAILABLE)
                .brand(volvo)
                .category(suv)
                .branch(branchSP)
                .features(List.of(f1, f2, f3, f4, f5, f6, f7))
                .build();
        v6.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=1200&q=80").isPrimary(true).vehicle(v6).build());
        vehicleRepository.save(v6);

        // Veículo 7: Ford Ranger Raptor
        Vehicle v7 = Vehicle.builder()
                .model("Ford Ranger Raptor 3.0 V6 Bi-Turbo")
                .yearManufacture(2024)
                .yearModel(2024)
                .price(new BigDecimal("460000.00"))
                .mileage(7000)
                .color("Laranja Terra")
                .fuelType(FuelType.GASOLINE)
                .transmission(TransmissionType.AUTOMATIC)
                .chassisNumber("6FPRANGERAPTOR2024")
                .status(VehicleStatus.AVAILABLE)
                .brand(ford)
                .category(picape)
                .branch(branchPR)
                .features(List.of(f2, f3, f4, f5, f6, f7))
                .build();
        v7.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1551830820-330a71b99659?auto=format&fit=crop&w=1200&q=80").isPrimary(true).vehicle(v7).build());
        vehicleRepository.save(v7);

        // Veículo 8: Toyota Corolla Altis Hybrid
        Vehicle v8 = Vehicle.builder()
                .model("Toyota Corolla Altis Premium Hybrid")
                .yearManufacture(2023)
                .yearModel(2023)
                .price(new BigDecimal("175000.00"))
                .mileage(28000)
                .color("Preto Eclipse")
                .fuelType(FuelType.HYBRID)
                .transmission(TransmissionType.CVT)
                .chassisNumber("9BRCOROLLAALTIS23")
                .status(VehicleStatus.AVAILABLE)
                .brand(toyota)
                .category(sedan)
                .branch(branchSP)
                .features(List.of(f2, f3, f4, f6))
                .build();
        v8.getImages().add(VehicleImage.builder().url("https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?auto=format&fit=crop&w=1200&q=80").isPrimary(true).vehicle(v8).build());
        vehicleRepository.save(v8);

        log.info("DataSeeder: Concluído com sucesso! 1 Admin, 2 Operadores, 2 Filiais, 7 Marcas, 5 Categorias, 7 Opcionais e 8 Veículos criados.");
    }
}

