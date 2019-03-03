package com.dawes.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dawes.modelo.AlumnoCursoVO;
import com.dawes.modelo.AlumnoVO;
import com.dawes.modelo.CursoVO;
import com.dawes.servicios.ServicioAlumno;
import com.dawes.servicios.ServicioCurso;

@Controller
@RequestMapping("/")
public class AlumnoController {

	@Autowired
	ServicioAlumno sa;
	
	@Autowired
	ServicioCurso sc;
	
	// Mapeo para el admin
	@RequestMapping("/admin/lista")
	public String lista(Model modelo) {
		modelo.addAttribute("lista", sa.findAll());
		return "/admin/lista";
	}
	
	@RequestMapping("/admin/formInserta")
	public String formInserta(Model modelo) {
		modelo.addAttribute("alumno", new AlumnoVO());
		return "/admin/formInsertaAlumno";
	}
	
	@RequestMapping("/admin/inserta")
	public String inserta(@ModelAttribute AlumnoVO alumno, Model modelo) {
		boolean error = false;
		try {
			sa.save(alumno);
			return "redirect:/admin/lista";
		} catch (Exception e) {
			error = true;
			modelo.addAttribute("error", error);
			return "redirect:/admin/lista";
		} finally {
			modelo.addAttribute("lista", sa.findAll());
		}
		
		
		//return "lista" + error;
	}
	
	@RequestMapping("/admin/eliminaAlumno")
	public String elimina(@RequestParam int idalumno, Model modelo) {
		sa.delete(sa.findById(idalumno).get());
		modelo.addAttribute("lista", sa.findAll());
		
		return "redirect:/admin/lista";
	}
	
	@RequestMapping("/admin/modificaAlumno")
	public String modifica(@RequestParam int idalumno, Model modelo) {
		modelo.addAttribute("alumno", sa.findById(idalumno).get());
		
		return "/admin/formModificaAlumno";
	}
	
	@RequestMapping("/admin/guardaAlumno")
	public String guarda(@ModelAttribute AlumnoVO alumno, Model modelo) {
		Optional<AlumnoVO> alumno_intermedio = sa.findById(alumno.getIdalumno());
		alumno.setCursos(alumno_intermedio.get().getCursos());
		sa.save(alumno);
		modelo.addAttribute("lista", sa.findAll());
		return "redirect:/admin/lista";
	}
	
	@RequestMapping("/admin/gestionarCursos")
	public String gestionarCursos(@RequestParam int idalumno, Model modelo) {
		modelo.addAttribute("alumno", sa.findById(idalumno).get());
		modelo.addAttribute("listaCurso", sc.findAll());
		return "/admin/gestionarCursos";
	}
	
	@RequestMapping("/admin/eliminarMatricula")
	public String eliminaMatricula(@RequestParam int idalumno, @RequestParam int idcurso, Model modelo) {
		AlumnoVO alumno = sa.findById(idalumno).get();
		CursoVO curso = sc.findById(idcurso).get();
		
		alumno.removeCurso(curso);
		
		sa.save(alumno);
		
		modelo.addAttribute("alumno", sa.findById(idalumno).get());
		modelo.addAttribute("listaCurso", sc.findAll());
		return "/admin/gestionarCursos";
	}
	
	@RequestMapping("/admin/matricularse")
	public String matricularse(@RequestParam int idalumno, @RequestParam int idcurso, Model modelo) {
		AlumnoVO alumno = sa.findById(idalumno).get();
		CursoVO curso = sc.findById(idcurso).get();
		
		Iterator<AlumnoCursoVO> it = alumno.getCursos().iterator();
		
		boolean resultado = false;
		String respuesta = "";
		while(it.hasNext()) {
				
			if (idcurso == it.next().getCurso().getIdcurso()) {
				resultado = true;
				respuesta = "Ya estas matriculado en '" + curso.getDenominacion() + "'";
			}
			
		}
		if (!resultado) {
			AlumnoCursoVO acv = new AlumnoCursoVO(LocalDate.now(), alumno, curso);
			alumno.addCurso(acv);
			respuesta = "";
		}

		sa.save(alumno);
		modelo.addAttribute("respuesta", respuesta);
		modelo.addAttribute("alumno", sa.findById(idalumno).get());
		modelo.addAttribute("listaCurso", sc.findAll());
		return "/admin/gestionarCursos";
	}
	
	//	Métodos solo para visualizar, mapeo para el usuario registrado
	@RequestMapping("/registrado/indexRegistrado")
	public String listaRegistrados(Model modelo, Principal principal) {
		String nombre = principal.getName();
		modelo.addAttribute("lista", sa.findAll());
		modelo.addAttribute("listaCurso", sc.findAll());
		modelo.addAttribute("nombre", nombre);
		return "/registrado/indexRegistrado";
	}
	
}
