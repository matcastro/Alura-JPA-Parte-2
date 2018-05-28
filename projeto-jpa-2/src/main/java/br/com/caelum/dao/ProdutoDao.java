package br.com.caelum.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.caelum.model.Loja;
import br.com.caelum.model.Produto;

@Repository
public class ProdutoDao {

	@PersistenceContext
	private EntityManager em;

	public List<Produto> getProdutos() {
		return em.createQuery("select distinct p from Produto p ", Produto.class)
				.setHint("javax.persistence.fetchgraph", em.getEntityGraph("produtoComCategoria"))
				.getResultList();
	}

	public Produto getProduto(Integer id) {
		Produto produto = em.find(Produto.class, id);
		return produto;
	}

	//@SuppressWarnings("unchecked")
	@Transactional
	public List<Produto> getProdutos(String nome, Integer categoriaId, Integer lojaId) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Produto> query = criteriaBuilder.createQuery(Produto.class);
		Root<Produto> root = query.from(Produto.class);

		Path<String> nomePath = root.<String>get("nome");
		Path<Integer> lojaPath = root.<Loja>get("loja").<Integer>get("id");
		Path<Integer> categoriaPath = root.join("categorias").<Integer>get("id");

		Predicate where = criteriaBuilder.conjunction();

		if (!nome.isEmpty()) {
			where = criteriaBuilder.and(where, criteriaBuilder.like(nomePath, "%" + nome + "%"));
		}
		if (categoriaId != null) {
			where = criteriaBuilder.and(where, criteriaBuilder.equal(categoriaPath, categoriaId));
		}
		if (lojaId != null) {
			where = criteriaBuilder.and(where, criteriaBuilder.equal(lojaPath, lojaId));
		}

		query.where(where).distinct(true);

		TypedQuery<Produto> typedQuery = em.createQuery(query);
		typedQuery.setHint("org.hibernate.cacheable", "true");
		return typedQuery.getResultList();
		/*
		Session session = em.unwrap(Session.class);
		Criteria criteria = session.createCriteria(Produto.class);
		
		if (!nome.isEmpty()) {
			criteria.add(Restrictions.like("nome", "%" + nome + "%"));
		}
		if (categoriaId != null) {
			criteria.setFetchMode("categorias", FetchMode.JOIN)
			.createAlias("categorias", "c")
			.add(Restrictions.like("c.id", categoriaId));
		}
		if (lojaId != null) {
			criteria.add(Restrictions.like("loja.id", lojaId));
		}

		return ((List<Produto>) criteria.list());
		*/
	}

	public void insere(Produto produto) {
		if (produto.getId() == null)
			em.persist(produto);
		else
			em.merge(produto);
	}

}
