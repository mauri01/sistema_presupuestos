package com.example.controller;

import com.example.model.*;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class LoginController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private ArticleService articleService;

	@Autowired
	private ProveedorService proveedorService;

	@Autowired
	private VentaService ventaService;

	@Autowired
	private NegocioService negocioService;

	@Autowired
	private ClienteService clienteService;

	@RequestMapping(value={"/", "/login"}, method = RequestMethod.GET)
	public ModelAndView login(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login");
		String nombreNegocio = "Sistema de VENTAS";

		Optional<Negocio> negocio = negocioService.findAll().stream().findFirst();
		if(negocio.isPresent()){
			nombreNegocio =negocio.get().getNombre().toUpperCase();
		}
		modelAndView.addObject("nombreNegocio", nombreNegocio);
		return modelAndView;
	}

	@RequestMapping(value="/firstRegistration", method = RequestMethod.GET)
	public ModelAndView firstRegistration(){
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		modelAndView.addObject("user", user);
		modelAndView.setViewName("firstRegistration");
		return modelAndView;
	}

	@RequestMapping(value="/registration", method = RequestMethod.GET)
	public ModelAndView registration(){
		ModelAndView modelAndView = new ModelAndView();
		User user = new User();
		modelAndView.addObject("user", user);
		modelAndView.setViewName("registration");
		return modelAndView;
	}

	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult,@RequestParam(value = "i", required=false) Integer i) {
		ModelAndView modelAndView = new ModelAndView();
		User userExists = userService.findUserByEmail(user.getEmail());
		if (userExists != null) {
			bindingResult
					.rejectValue("email", "error.user",
							"There is already a user registered with the email provided");
		}
		if (bindingResult.hasErrors()) {
			if(i != null){
				modelAndView.addObject("errorMessage", "El mail ingresado ya existe en el sistema. Intente nuevamente");
				modelAndView.setViewName("firstRegistration");
			}else{
				modelAndView.setViewName("registration");
			}
		} else {
			userService.saveUser(user);
			modelAndView.addObject("successMessage", "El usuario fue registrado exitosamente.");
			modelAndView.addObject("user", new User());
			if(i != null){
				modelAndView.setViewName("firstRegistration");
			}else{
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
	
	@RequestMapping(value="/admin/home", method = RequestMethod.GET)
	public ModelAndView home() throws ParseException {
		ModelAndView modelAndView = new ModelAndView();
		int countVentas = 0;
		DateFormat hourdateFormat = new SimpleDateFormat("MMMM");
		String fechaMes = hourdateFormat.format(new Date());

		countVentas = getCountVentasFecha(countVentas);
		String stockDisponible = getStockDisponible();

		List<Article> allArticles = articleService.findAllArticleActive();
		List<Cliente> clientes = clienteService.findAll();

		modelAndView.addObject("provTotal",proveedorService.findAll());
		modelAndView.addObject("countVentas", countVentas);
		modelAndView.addObject("fechaMes",fechaMes);
		modelAndView.addObject("articles", allArticles);
		modelAndView.addObject("stockDisponible", stockDisponible);
		modelAndView.addObject("clientes", clientes.stream()
																.filter(Cliente::isActive)
																.collect(Collectors.toList()));
		modelAndView.setViewName("admin/index");
		return modelAndView;
	}

	public String getStockDisponible() {
		String stockMessage = "-";
		List<Article> stockDisponible = articleService.findAllArticleActive().stream()
				.filter(article -> article.getStock() > 0).collect(Collectors.toList());
		if(stockDisponible.size() != 0){
			stockMessage = "+";
		}
		return stockMessage;
	}

	public int getCountVentasFecha(int countVentas) throws ParseException {
		DateFormat hourdateFormat = new SimpleDateFormat("MM");
		String fechaMes = hourdateFormat.format(new Date());
		DateFormat hourdateFormatAnio = new SimpleDateFormat("yyyy");
		String fechaAnio = hourdateFormatAnio.format(new Date());

		int idPedido = 0;
		List<Venta> listVentas = ventaService.findAll();

		for (Venta venta : listVentas){
			if(venta.getPedido() != idPedido){
				Date dateVenta = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").parse(venta.getFechaVenta());
				String fechaMesVenta = hourdateFormat.format(dateVenta);
				String fechaAnioVenta = hourdateFormatAnio.format(dateVenta);

				if(fechaMes.equals(fechaMesVenta) && fechaAnio.equals(fechaAnioVenta)){
					countVentas++;
				}
				idPedido = venta.getPedido();
			}
		}
		return countVentas;
	}

	@RequestMapping(value="/admin/document", method = RequestMethod.GET)
	public ModelAndView versionView(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("version");
		return modelAndView;
	}

	@RequestMapping(value="/admin/faq", method = RequestMethod.GET)
	public ModelAndView faq(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("faq");
		return modelAndView;
	}

	@RequestMapping(value="/admin/user", method = RequestMethod.GET)
	public ModelAndView user(){
		ModelAndView modelAndView = new ModelAndView();
		List<User> listUser = userService.findAllUser();
		modelAndView.addObject("users", listUser);
		modelAndView.setViewName("admin/user");
		return modelAndView;
	}

	@GetMapping("/user/{id}/remove")
	public ModelAndView removeUser(@PathVariable("id") int id){
		User user = userService.findUserById(id);
		userService.softDeleteUser(user);
		return new ModelAndView("redirect:/admin/user");
	}

	@GetMapping("/user/{id}/edit")
	public ModelAndView editUser(@PathVariable("id") int id){
		ModelAndView modelAndView = new ModelAndView();
		User user = userService.findUserById(id);
		List<Role> roles = userService.finAllRoles();
		modelAndView.addObject("userEdit", user);
		modelAndView.addObject("roles", roles);
		modelAndView.setViewName("registration");
		return modelAndView;
	}

	@RequestMapping(value="/admin/article/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Article articles(@PathVariable("id") int id){
		return articleService.findbyId(id);
	}

	@RequestMapping(value="/admin/article", method = RequestMethod.GET)
	public ModelAndView articles(){
		ModelAndView modelAndView = new ModelAndView();
		Article article = new Article();
		modelAndView.addObject("article", article);
		modelAndView.setViewName("admin/article");
		return modelAndView;
	}

	@RequestMapping(value="/article", method = RequestMethod.POST)
	public ModelAndView addArticle(@Valid Article article){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("admin/article");
		String messageError = null;
		String message = null;
		boolean noExisteCodigo = true;

		Article articleCreated = articleService.findbyName(article.getNombre());
		if(!article.getCodigoBarra().equals("0") && !article.getCodigoBarra().equals("")){
			Optional<Article> articleBarra = articleService.findAllArticle()
					.stream().filter(articlebd -> articlebd.getCodigoBarra() == article.getCodigoBarra()).findFirst();

			if(articleBarra.isPresent()){
				noExisteCodigo = false;
			}
		}

		if(articleCreated == null && noExisteCodigo){
			article.setPrecioVenta(Float.parseFloat("0"));
			articleService.saveArticle(article);
			message = "El Artículo fue creado de forma correcta";
		}else if(!articleCreated.isActive()){
			articleCreated.setActive(true);
			articleService.saveArticle(articleCreated);
			message = "El Artículo YA existía, fue activado nuevamente.";
		}else{
			messageError = "El nombre del Artículo o codigo de barra ya existe.";
		}
		modelAndView.addObject("error", messageError);
		modelAndView.addObject("message", message);
		return modelAndView;
	}

	@RequestMapping(value="/error", method = RequestMethod.GET)
	public ModelAndView error(){
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("error");
		return modelAndView;
	}

}
