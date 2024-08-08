package com.example.controller;

import com.example.model.*;
import com.example.repository.RoleRepository;
import com.example.service.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private RoleRepository roleRepository;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private NegocioService negocioService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PriceListService priceListService;

    @RequestMapping(value = {"/", "/login"}, method = RequestMethod.GET)
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        String nombreNegocio = "Sistema de PRESUPUESTOS";

        Optional<Negocio> negocio = negocioService.findAll().stream().findFirst();
        if (negocio.isPresent()) {
            nombreNegocio = negocio.get().getNombre().toUpperCase();
        }
        modelAndView.addObject("nombreNegocio", nombreNegocio);
        return modelAndView;
    }

    @RequestMapping(value = "/firstRegistration", method = RequestMethod.GET)
    public ModelAndView firstRegistration() {
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("firstRegistration");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public ModelAndView registration() {
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult, @RequestParam(value = "i", required = false) Integer i) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserByEmail(user.getEmail());
        if (userExists != null) {
            bindingResult
                    .rejectValue("email", "error.user",
                            "There is already a user registered with the email provided");
        }
        if (bindingResult.hasErrors()) {
            if (i != null) {
                modelAndView.addObject("errorMessage", "El mail ingresado ya existe en el sistema. Intente nuevamente");
                modelAndView.setViewName("firstRegistration");
            } else {
                modelAndView.setViewName("registration");
            }
        } else {
            Role userRole = roleRepository.findByRole("USER");
            user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "El usuario fue registrado exitosamente.");
            modelAndView.addObject("user", new User());
            if (i != null) {
                modelAndView.setViewName("firstRegistration");
            } else {
                modelAndView.setViewName("registration");
            }

        }
        return modelAndView;
    }

    @RequestMapping(value = "/editUser", method = RequestMethod.POST)
    public ModelAndView editUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserById(user.getId());
        if (userExists != null) {
            userService.updateUser(user);
        }
        if (bindingResult.hasErrors()) {
            //modelAndView.setViewName("registration");

        }
        return new ModelAndView("redirect:/admin/user");
    }

    @RequestMapping(value = "/admin/home", method = RequestMethod.GET)
    public ModelAndView home() throws ParseException, IOException {
        ModelAndView modelAndView = new ModelAndView();
        int countVentas = 0;
        DateFormat hourdateFormat = new SimpleDateFormat("MMMM");
        String fechaMes = hourdateFormat.format(new Date());
        User user = getUserAuth();
        PriceList priceListFromUser = priceListService.findByUserId((long) user.getId());
        List<Price> priceList = null;
        if (Objects.nonNull(priceListFromUser)) {
            priceList = priceListService.findExcelPrices((long) user.getId());
        }

        countVentas = getCountVentasFecha(countVentas);
        List<Cliente> clientes = clienteService.findAll();
        modelAndView.addObject("countVentas", countVentas);
        modelAndView.addObject("fechaMes", fechaMes);
        modelAndView.addObject("clientes", clientes.stream()
                .filter(cliente -> cliente.getFromUser() == user.getId() && cliente.isActive())
                .collect(Collectors.toList()));
        modelAndView.addObject("priceList", priceList);

        if (Objects.isNull(priceList)) {
            modelAndView.addObject("messageLista", "No cuenta con Lista de Precios cargada.");
        }
        modelAndView.setViewName("admin/index");
        return modelAndView;
    }

    public int getCountVentasFecha(int countVentas) throws ParseException {
        DateFormat hourdateFormat = new SimpleDateFormat("MM");
        String fechaMes = hourdateFormat.format(new Date());
        DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");
        String fechaAnio = hourdateFormatAnio.format(new Date());

        int idPedido = 0;
        List<Venta> listVentas = ventaService.findAll();

        for (Venta venta : listVentas) {
            if (venta.getPedido() != idPedido) {
                Date dateVenta = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(venta.getFechaVenta());
                String fechaMesVenta = hourdateFormat.format(dateVenta);
                String fechaAnioVenta = hourdateFormatAnio.format(dateVenta);

                if (fechaMes.equals(fechaMesVenta) && fechaAnio.equals(fechaAnioVenta)) {
                    countVentas++;
                }
                idPedido = venta.getPedido();
            }
        }
        return countVentas;
    }

    @RequestMapping(value = "/admin/document", method = RequestMethod.GET)
    public ModelAndView versionView() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("version");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/faq", method = RequestMethod.GET)
    public ModelAndView faq() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("faq");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/priceList", method = RequestMethod.GET)
    public ModelAndView priceList() throws IOException {
        ModelAndView modelAndView = new ModelAndView();
        User user = getUserAuth();
        modelAndView.addObject("userId", user.getId());
        modelAndView.setViewName("admin/priceList");
        return modelAndView;
    }

    private User getUserAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();

        } else if (authentication != null) {
            username = authentication.getPrincipal().toString();
        }
        return userService.findUserByEmail(username);
    }

    @RequestMapping(value = "/upload-price", method = RequestMethod.POST)
    public ModelAndView addPriceList(@RequestParam("file") MultipartFile file,
                                     @RequestParam("userIdFromPriceList") Long userId) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("admin/priceList");

        if (file.isEmpty()) {
            modelAndView.addObject("error", "Debe Cargar un archivo.");
            return modelAndView;
        }

        // Validar tipo de archivo
        String fileType = file.getContentType();
        if (!fileType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            modelAndView.addObject("error", "Debe Cargar un archivo de Excel.");
            return modelAndView;
        }

        try {
            // Leer el contenido del archivo
            try (InputStream is = file.getInputStream()) {
                Workbook workbook = new XSSFWorkbook(is);
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                if (headerRow == null || headerRow.getPhysicalNumberOfCells() < 3) {
                    modelAndView.addObject("error", "Debe Cargar un archivo de Excel con las columnas requeridas.");
                    return modelAndView;
                }

                String idColumn = headerRow.getCell(0).getStringCellValue();
                String nameColumn = headerRow.getCell(1).getStringCellValue();
                String precioColumn = headerRow.getCell(2).getStringCellValue();
                String unidadColumn = headerRow.getCell(3).getStringCellValue();

                if (!"id".equalsIgnoreCase(idColumn) || !"nombre".equalsIgnoreCase(nameColumn) || !"precio".equalsIgnoreCase(precioColumn) || !"unidad".equalsIgnoreCase(unidadColumn)) {
                    modelAndView.addObject("error", "Debe Cargar un archivo de Excel con el nombre de las columnas requeridas.");
                    return modelAndView;
                }
            }

            String fileName = file.getOriginalFilename();
            // Truncar el nombre del archivo si es necesario
            if (fileName.length() > 255) {
                fileName = fileName.substring(0, 255);
            }

            PriceList fileStorage = new PriceList();
            fileStorage.setUserId(userId);
            fileStorage.setData(file.getBytes());
            fileStorage.setNameFile(fileName);

            priceListService.save(fileStorage);

        } catch (IOException e) {
            modelAndView.addObject("error", "Error guardando el archivo, error:" + e.getMessage());
            return modelAndView;
        }

        modelAndView.addObject("success", "El archivo se registro de forma correcta.");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/user", method = RequestMethod.GET)
    public ModelAndView user() {
        ModelAndView modelAndView = new ModelAndView();
        List<User> listUser = userService.findAllUser();
        modelAndView.addObject("users", listUser);
        modelAndView.setViewName("admin/user");
        return modelAndView;
    }

    @GetMapping("/user/{id}/remove")
    public ModelAndView removeUser(@PathVariable("id") int id) {
        User user = userService.findUserById(id);
        userService.softDeleteUser(user);
        return new ModelAndView("redirect:/admin/user");
    }

    @GetMapping("/user/{id}/edit")
    public ModelAndView editUser(@PathVariable("id") int id) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.findUserById(id);
        List<Role> roles = userService.finAllRoles();
        modelAndView.addObject("userEdit", user);
        modelAndView.addObject("roles", roles);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @RequestMapping(value = "/admin/article/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Price articles(@PathVariable("id") int id) throws IOException {
        User user = getUserAuth();
        List<Price> priceList = priceListService.findExcelPrices((long) user.getId());
        return priceList.stream().filter(price -> price.getId() == id).findFirst().orElse(null);
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public ModelAndView error() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error");
        return modelAndView;
    }

}
