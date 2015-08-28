--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: address_reference_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE address_reference_type AS ENUM (
    'conditional_true',
    'conditional_false',
    'unconditional',
    'switch',
    'call_direct',
    'call_indirect',
    'call_virtual',
    'data',
    'data_string'
);


ALTER TYPE public.address_reference_type OWNER TO postgres;

--
-- Name: TYPE address_reference_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE address_reference_type IS 'The address_reference_type defines all possible address reference types.';


--
-- Name: architecture_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE architecture_type AS ENUM (
    'x86-32',
    'x86-64',
    'ARM-32',
    'PowerPC-32',
    'PowerPC-64',
    'MIPS-32',
    'MIPS-64',
    'GENERIC-32',
    'GENERIC-64',
    'REIL',
    'RREIL'
);


ALTER TYPE public.architecture_type OWNER TO postgres;

--
-- Name: TYPE architecture_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE architecture_type IS 'The architecture_type defines all architectures known to BinNavi.
  Unknown architectures used the generic type.';


--
-- Name: edge_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE edge_type AS ENUM (
    'jump_conditional_true',
    'jump_conditional_false',
    'jump_unconditional',
    'jump_switch',
    'jump_conditional_true_loop',
    'jump_conditional_false_loop',
    'jump_unconditional_loop',
    'enter_inlined_function',
    'leave_inlined_function',
    'inter_module',
    'inter_addressspace_edge',
    'textnode_edge',
    'dummy'
);


ALTER TYPE public.edge_type OWNER TO postgres;

--
-- Name: TYPE edge_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE edge_type IS 'The edge_type defines all possible types of an edge.
  This type is used in BinNavi to enable specific functions for an edge.';


--
-- Name: ex_1_section_permission_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE ex_1_section_permission_type AS ENUM (
    'READ',
    'WRITE',
    'EXECUTE',
    'READ_WRITE',
    'READ_EXECUTE',
    'WRITE_EXECUTE',
    'READ_WRITE_EXECUTE'
);


ALTER TYPE public.ex_1_section_permission_type OWNER TO postgres;

--
-- Name: ex_1_type_category; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE ex_1_type_category AS ENUM (
    'atomic',
    'pointer',
    'array',
    'struct',
    'union',
    'function_pointer'
);


ALTER TYPE public.ex_1_type_category OWNER TO postgres;

--
-- Name: ex_1_type_renderers_renderer_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE ex_1_type_renderers_renderer_type AS ENUM (
    'integer',
    'floating point',
    'boolean',
    'ascii',
    'utf8',
    'utf16'
);


ALTER TYPE public.ex_1_type_renderers_renderer_type OWNER TO postgres;

--
-- Name: ex_2_section_permission_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE ex_2_section_permission_type AS ENUM (
    'READ',
    'WRITE',
    'EXECUTE',
    'READ_WRITE',
    'READ_EXECUTE',
    'WRITE_EXECUTE',
    'READ_WRITE_EXECUTE'
);


ALTER TYPE public.ex_2_section_permission_type OWNER TO postgres;

--
-- Name: ex_2_type_category; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE ex_2_type_category AS ENUM (
    'atomic',
    'pointer',
    'array',
    'struct',
    'union',
    'function_pointer'
);


ALTER TYPE public.ex_2_type_category OWNER TO postgres;

--
-- Name: ex_2_type_renderers_renderer_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE ex_2_type_renderers_renderer_type AS ENUM (
    'integer',
    'floating point',
    'boolean',
    'ascii',
    'utf8',
    'utf16'
);


ALTER TYPE public.ex_2_type_renderers_renderer_type OWNER TO postgres;

--
-- Name: function_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE function_type AS ENUM (
    'normal',
    'library',
    'import',
    'thunk',
    'adjustor_thunk',
    'invalid'
);


ALTER TYPE public.function_type OWNER TO postgres;

--
-- Name: TYPE function_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE function_type IS 'The function_type defines all possible function types.
  This type is used in BinNavi to enable specific functions for a function.';


--
-- Name: node_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE node_type AS ENUM (
    'code',
    'function',
    'group',
    'text'
);


ALTER TYPE public.node_type OWNER TO postgres;

--
-- Name: TYPE node_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE node_type IS 'The node_type defines all possible node type.
  The type is used in BinNavi to enable specific functions for a node.';


--
-- Name: permission_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE permission_type AS ENUM (
    'READ',
    'WRITE',
    'EXECUTE',
    'READ_WRITE',
    'READ_EXECUTE',
    'READ_WRITE_EXECUTE',
    'WRITE_EXECUTE'
);


ALTER TYPE public.permission_type OWNER TO postgres;

--
-- Name: TYPE permission_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE permission_type IS 'The permission_type is used to describe the permission a section has.
  This information can either come from the exporter or from the debugger.';


--
-- Name: tag_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE tag_type AS ENUM (
    'view_tag',
    'node_tag'
);


ALTER TYPE public.tag_type OWNER TO postgres;

--
-- Name: TYPE tag_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE tag_type IS 'The tag_type defines to which taggable instance the tags belongs.
  A tag can either be a view tag or a node tag.';


--
-- Name: type_category; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE type_category AS ENUM (
    'atomic',
    'array',
    'pointer',
    'struct',
    'union',
    'function_pointer'
);


ALTER TYPE public.type_category OWNER TO postgres;

--
-- Name: TYPE type_category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE type_category IS 'The type_category enum specifies the category of a given base type as defined in a C type system.';


--
-- Name: view_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE view_type AS ENUM (
    'native',
    'non-native'
);


ALTER TYPE public.view_type OWNER TO postgres;

--
-- Name: TYPE view_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TYPE view_type IS 'The view_type defines where a view comes from.
  Native views come from the export / disassembler.
  Non-native views have been generated in BinNavi.
  Native views are immutable.';


--
-- Name: append_comment(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_comment(parent_id integer, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
  DECLARE
    comment_id integer;
    child integer;
  BEGIN
    --
    -- Find out if the node in question has a child
    -- node.
    --
    SELECT two.id INTO child FROM bn_comments AS one
      JOIN bn_comments AS two
        ON (one.id = two.parent_id)
    WHERE one.id = $1;

    --
    -- If we find a child node we must exit as append
    -- only works on a node which has no child.
    --
    IF FOUND THEN
      RAISE EXCEPTION 'can not append comment to node % as it has a child comment', $1;
    END IF;

    --
    -- If we did not find a child node we perform the append
    -- operation and return the newly generated
    --
    INSERT INTO bn_comments(id, parent_id, user_id, comment_text)
      VALUES (nextval('bn_comments_id_seq'::regclass), $1, $2, $3)
      RETURNING id INTO STRICT comment_id;

    RETURN comment_id;

  END;
 $_$;


ALTER FUNCTION public.append_comment(parent_id integer, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_comment(parent_id integer, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_comment(parent_id integer, user_id integer, comment_text text) IS 'This function appends a comment to a comment owned by the user given as argument.
  It returns the generated id of the comment.
  This function is used by all other append comment functions.';


--
-- Name: append_function_comment(integer, bigint, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_function_comment(moduleid integer, functionaddress bigint, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the function already has a comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_functions
     WHERE module_id = moduleid
       AND address = functionaddress;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- Update the function nodes table with the newly generated comment.
    --
    UPDATE bn_functions
      SET comment_id = commentid
    WHERE module_id = moduleid
      AND address = functionaddress;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_function_comment(moduleid integer, functionaddress bigint, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_function_comment(moduleid integer, functionaddress bigint, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_function_comment(moduleid integer, functionaddress bigint, user_id integer, comment_text text) IS 'This function appends a comment to a function. It returns the generated id of the comment.';


--
-- Name: append_function_node_comment(integer, integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_function_node_comment(moduleid integer, nodeid integer, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the function already has a comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_function_nodes
     WHERE node_id = nodeid
      AND module_id = moduleid;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- Update the function nodes table with the newly generated comment.
    --
    UPDATE bn_function_nodes
      SET comment_id = commentid
    WHERE node_id = nodeid
      AND module_id = moduleid;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_function_node_comment(moduleid integer, nodeid integer, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_function_node_comment(moduleid integer, nodeid integer, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_function_node_comment(moduleid integer, nodeid integer, user_id integer, comment_text text) IS 'This function appends a comment to a function node. It returns the generated id of the comment.';


--
-- Name: append_global_code_node_comment(integer, integer, bigint, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_global_code_node_comment(moduleid integer, node_id integer, node_address bigint, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the code node already has a global comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_global_node_comments
     WHERE module_id = moduleid
       AND address = node_address;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- We use the information from the parent id to decide
    -- if we need to update or if we need to insert.
    --
    IF (parentid IS NOT NULL) THEN
      UPDATE bn_global_node_comments
         SET comment_id = commentid
       WHERE module_id = moduleid
         AND address = node_address
         AND comment_id = parentid;
    ELSE
      INSERT INTO bn_global_node_comments
        VALUES(moduleid, node_address, commentid);
    END IF;
    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_global_code_node_comment(moduleid integer, node_id integer, node_address bigint, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_global_code_node_comment(moduleid integer, node_id integer, node_address bigint, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_global_code_node_comment(moduleid integer, node_id integer, node_address bigint, user_id integer, comment_text text) IS 'This function appends a global code node comment. It returns the generated id of the comment.';


--
-- Name: append_global_edge_comment(integer, integer, bigint, bigint, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_global_edge_comment(srcmoduleid integer, dstmoduleid integer, srcnodeaddress bigint, dstnodeaddress bigint, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the edge already has a global comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_global_edge_comments
     WHERE src_module_id = srcmoduleid
       AND dst_module_id = dstmoduleid
       AND src_address = srcnodeaddress
       AND dst_address = dstnodeaddress;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

     --
    -- We use the information from the parent id to decide
    -- if we need to update or if we need to insert.
    --
    IF (parentid IS NOT NULL) THEN
      UPDATE bn_global_edge_comments
         SET comment_id = commentid
       WHERE src_module_id = srcmoduleid
         AND dst_module_id = dstmoduleid
         AND src_address = srcnodeaddress
         AND dst_address = dstnodeaddress
         AND comment_id = parentid;
    ELSE
      INSERT INTO bn_global_edge_comments
        VALUES(srcmoduleid, dstmoduleid, srcnodeaddress, dstnodeaddress, commentid);
    END IF;
    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_global_edge_comment(srcmoduleid integer, dstmoduleid integer, srcnodeaddress bigint, dstnodeaddress bigint, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_global_edge_comment(srcmoduleid integer, dstmoduleid integer, srcnodeaddress bigint, dstnodeaddress bigint, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_global_edge_comment(srcmoduleid integer, dstmoduleid integer, srcnodeaddress bigint, dstnodeaddress bigint, user_id integer, comment_text text) IS 'This function appends a global edge comment. It returns the generated id of the comment.';


--
-- Name: append_global_instruction_comment(integer, bigint, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_global_instruction_comment(moduleid integer, instruction_address bigint, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the instruction already has a global comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_instructions
     WHERE module_id = moduleid
       AND address = instruction_address;
    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- Update the record of the instruction to point to the
    -- new comment.
    --
    UPDATE bn_instructions
       SET comment_id = commentid
     WHERE module_id = moduleid
       AND address = instruction_address;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_global_instruction_comment(moduleid integer, instruction_address bigint, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_global_instruction_comment(moduleid integer, instruction_address bigint, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_global_instruction_comment(moduleid integer, instruction_address bigint, user_id integer, comment_text text) IS 'This function appends a global instruction comment. It returns the generated id of the comment.';


--
-- Name: append_group_node_comment(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_group_node_comment(nodeid integer, userid integer, comment text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the group node already has a comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_group_nodes
     WHERE node_id = nodeId;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, userId, comment)
      INTO commentid;

    --
    -- Update the group nodes with the newly generated comment.
    --
    UPDATE bn_group_nodes
      SET comment_id = commentid
    WHERE node_id = nodeId;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_group_node_comment(nodeid integer, userid integer, comment text) OWNER TO postgres;

--
-- Name: FUNCTION append_group_node_comment(nodeid integer, userid integer, comment text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_group_node_comment(nodeid integer, userid integer, comment text) IS 'This function appends a comment to a group node. It returns the generated id of the comment.';


--
-- Name: append_local_code_node_comment(integer, integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_local_code_node_comment(moduleid integer, nodeid integer, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Find out if our current code node already
    -- had a local comment and save its comment_id.
    --
    SELECT comment_id INTO parentid
      FROM bn_code_nodes
     WHERE module_id = moduleid
       AND node_id = nodeid;

    --
    -- Create a comment using the append comment function
    -- with the optional current_node_comment_id as the new paremt.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- Update the code node with the newly generated comment.
    --
    UPDATE bn_code_nodes
       SET comment_id = commentid
     WHERE module_id = moduleid
       AND node_id = nodeid;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_local_code_node_comment(moduleid integer, nodeid integer, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_local_code_node_comment(moduleid integer, nodeid integer, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_local_code_node_comment(moduleid integer, nodeid integer, user_id integer, comment_text text) IS 'This function appends a local comment to a code node. It returns the generated id of the comment.';


--
-- Name: append_local_edge_comment(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_local_edge_comment(edge_id integer, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Find out if our current code node already
    -- had a local comment and save its comment_id.
    --
    SELECT comment_id INTO parentid
      FROM bn_edges
     WHERE id = edge_id;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- Update the edge with the newly generated comment.
    --
    UPDATE bn_edges
      SET comment_id = commentid
    WHERE id = edge_id;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_local_edge_comment(edge_id integer, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_local_edge_comment(edge_id integer, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_local_edge_comment(edge_id integer, user_id integer, comment_text text) IS 'This function appends a local edge comment to an edge. It returns the generated id of the comment.';


--
-- Name: append_local_instruction_comment(integer, integer, bigint, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_local_instruction_comment(moduleid integer, nodeid integer, instruction_address bigint, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Find out if our current code node already
    -- had a local comment and save its comment_id.
    --
    SELECT comment_id INTO parentid
      FROM bn_codenode_instructions
     WHERE module_id = moduleid
       AND node_id = nodeid
       AND address = instruction_address;
    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- Update the record of the instruction to point to the
    -- new comment.
    --
    UPDATE bn_codenode_instructions
       SET comment_id = commentid
     WHERE module_id = moduleid
       AND node_id = nodeid
       AND address = instruction_address;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_local_instruction_comment(moduleid integer, nodeid integer, instruction_address bigint, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_local_instruction_comment(moduleid integer, nodeid integer, instruction_address bigint, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_local_instruction_comment(moduleid integer, nodeid integer, instruction_address bigint, user_id integer, comment_text text) IS 'This function appends a local instruction comment to an instruction in a code node. It returns the generated id of the comment.';


--
-- Name: append_section_comment(integer, integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_section_comment(moduleid integer, sectionid integer, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the section already has a comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_sections
     WHERE module_id = moduleid
       AND id = sectionid;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- Update the sections table with the newly generated comment.
    --
    UPDATE bn_sections
       SET comment_id = commentid
     WHERE module_id = moduleid
       AND id = sectionid;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_section_comment(moduleid integer, sectionid integer, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_section_comment(moduleid integer, sectionid integer, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_section_comment(moduleid integer, sectionid integer, user_id integer, comment_text text) IS 'This function appends a comment to a section. It returns the generated id of the comment.';


--
-- Name: append_text_node_comment(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_text_node_comment(nodeid integer, userid integer, comment text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the text node already has a comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_text_nodes
     WHERE node_id = nodeId;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, userId, comment)
      INTO commentid;

    --
    -- Update the group nodes with the newly generated comment.
    --
    UPDATE bn_text_nodes
      SET comment_id = commentid
    WHERE node_id = nodeId;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_text_node_comment(nodeid integer, userid integer, comment text) OWNER TO postgres;

--
-- Name: FUNCTION append_text_node_comment(nodeid integer, userid integer, comment text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_text_node_comment(nodeid integer, userid integer, comment text) IS 'This function appends a comment to a text node. It returns the generated id of the comment.';


--
-- Name: append_type_instance_comment(integer, integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION append_type_instance_comment(moduleid integer, typeinstanceid integer, user_id integer, comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    commentid integer;
    parentid integer;
  BEGIN
    --
    -- Check if the type instance already has a comment
    -- associated with it.
    --
    SELECT comment_id INTO parentid
      FROM bn_type_instances
     WHERE module_id = moduleid
       AND id = typeinstanceid;

    --
    -- Create a comment using the append comment function.
    --
    SELECT append_comment(parentid, user_id, comment_text)
      INTO commentid;

    --
    -- Update the type instances table with the newly generated comment.
    --
    UPDATE bn_type_instances
       SET comment_id = commentid
     WHERE module_id = moduleid
       AND id = typeinstanceid;

    RETURN commentid;
  END;
$$;


ALTER FUNCTION public.append_type_instance_comment(moduleid integer, typeinstanceid integer, user_id integer, comment_text text) OWNER TO postgres;

--
-- Name: FUNCTION append_type_instance_comment(moduleid integer, typeinstanceid integer, user_id integer, comment_text text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION append_type_instance_comment(moduleid integer, typeinstanceid integer, user_id integer, comment_text text) IS 'This function appends a comment to a type instance. It returns the generated id of the comment.';


--
-- Name: bn_base_types_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_base_types_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.module_id || ' ' || OLD.id );
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_base_types_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_base_types_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_base_types_trigger() IS 'The bn_base_types_trigger is called for all altering operations on the bn_base_types table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi';


--
-- Name: bn_code_node_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_code_node_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  comment text;
BEGIN
  IF ( TG_OP = 'UPDATE') THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.node_id || ' ' || NEW.parent_function || ' ' || comment );
    RETURN NEW;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_code_node_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_code_node_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_code_node_comment_trigger() IS 'The bn_bn_code_node_comment_trigger is called for UPDATE operations on the bn_code_nodes table an will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of local comments associated to code nodes between multiple instances of BinNavi.';


--
-- Name: bn_codenode_instructions_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_codenode_instructions_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  comment text;
BEGIN
  IF ( TG_OP = 'UPDATE') THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.node_id || ' ' || NEW.position || ' ' || NEW.address || ' ' || comment );
    RETURN NEW;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_codenode_instructions_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_codenode_instructions_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_codenode_instructions_comment_trigger() IS 'The bn_codenode_instructions_comment_trigger is called for UPDATE operations on the bn_codenode_instructions table and will perform a pg_notify with the altered information
  This information is used in BinNavi to provide synchronisation of local comments associated to instructions between multiple instances of BinNavi.';


--
-- Name: bn_comments_audit_logger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_comments_audit_logger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
  BEGIN
    --
    -- Create a row in bn_comments_audit to reflect the operation performed on bn_comments,
    --
    IF (TG_OP = 'DELETE') THEN
      INSERT INTO bn_comments_audit SELECT 'D', now(), OLD.id, OLD.parent_id, OLD.user_id, OLD.comment_text;
      RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
      INSERT INTO bn_comments_audit SELECT 'U', now(), NEW.id, NEW.parent_id, NEW.user_id, NEW.comment_text;
      RETURN NEW;
    ELSIF (TG_OP = 'INSERT') THEN
      INSERT INTO bn_comments_audit SELECT 'I', now(), NEW.id, NEW.parent_id, NEW.user_id, NEW.comment_text;
      RETURN NEW;
    END IF;
    RETURN NULL;
  END;
$$;


ALTER FUNCTION public.bn_comments_audit_logger() OWNER TO postgres;

--
-- Name: FUNCTION bn_comments_audit_logger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_comments_audit_logger() IS 'The bn_comments_audit_logger is called for all operations performed on the bn_comments table and saves the operation with the altered information in the table
  bn_comments_audit. This information can be used to track changes to comments which have been performed to a database over time.';


--
-- Name: bn_comments_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_comments_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  comment text;
  parentid text;
BEGIN
  IF ( TG_OP = 'UPDATE') THEN
    IF ( NEW.comment_text IS NULL) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_text AS TEXT);
    END IF;
    IF( NEW.parent_id IS NULL) THEN
      parentid = 'null';
    ELSE
      parentid = CAST(NEW.parent_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.id || ' ' || parentid || ' ' || NEW.user_id || ' ' || comment );
    RETURN NEW;
  ELSIF (TG_OP = 'DELETE') THEN
    IF ( OLD.comment_text IS NULL) THEN
      comment = 'null';
    ELSE
      comment = CAST(OLD.comment_text AS TEXT);
    END IF;
    IF( OLD.parent_id IS NULL) THEN
      parentid = 'null';
    ELSE
      parentid = CAST(OLD.parent_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.id || ' ' || parentid || ' ' || OLD.user_id || ' ' || comment );
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_comments_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_comments_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_comments_trigger() IS 'The bn_comments_trigger is called for UPDATE and DELETE operations on the bn_comments table and will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments between multiple instances of BinNavi.';


--
-- Name: bn_edges_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_edges_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
  DECLARE
  comment text;
  BEGIN
  IF ( TG_OP = 'UPDATE') THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.id || ' ' || comment );
    RETURN NEW;
  END IF;
  END;
  $$;


ALTER FUNCTION public.bn_edges_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_edges_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_edges_comment_trigger() IS 'The bn_edges_comment_trigger is called for UPDATE operations on the bn_edges table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of local comments associated to edges between multiple instances of BinNavi.';


--
-- Name: bn_expression_type_instances_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_expression_type_instances_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('type_instances_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address || ' ' || NEW."position" || ' ' || NEW.expression_id || ' ' || NEW.type_instance_id);
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('type_instances_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address || ' ' || NEW."position" || ' ' || NEW.expression_id || ' ' || NEW.type_instance_id);
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('type_instances_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.module_id || ' ' || OLD.address || ' ' || OLD."position" || ' ' || OLD.expression_id || ' ' || OLD.type_instance_id);
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_expression_type_instances_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_expression_type_instances_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_expression_type_instances_trigger() IS 'The bn_expression_type_instances_trigger is called for all operations on the bn_expression_type_instances table an will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronization of type instances associated to type instances between multiple instances of BinNavi.';


--
-- Name: bn_expression_types_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_expression_types_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address || ' ' || NEW."position" || ' ' || NEW.expression_id);
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address || ' ' || NEW."position" || ' ' || NEW.expression_id);
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.module_id || ' ' || OLD.address || ' ' || OLD."position" || ' ' || OLD.expression_id);
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_expression_types_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_expression_types_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_expression_types_trigger() IS 'The bn_expression_types_trigger is called for all altering operations on the bn_expression_types table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronization between multiple instances of BinNavi';


--
-- Name: bn_function_nodes_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_function_nodes_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
  DECLARE
  comment text;
  BEGIN
  IF ( TG_OP = 'UPDATE') THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.node_id || ' ' || NEW.function || ' ' || comment );
    RETURN NEW;
  END IF;
  END;
  $$;


ALTER FUNCTION public.bn_function_nodes_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_function_nodes_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_function_nodes_comment_trigger() IS 'The bn_function_nodes_comment_trigger is called for UPDATE operations on the bn_function_nodes table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to function nodes between multiple instances of BinNavi.';


--
-- Name: bn_functions_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_functions_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
 DECLARE
  comment text;
 BEGIN
  IF ( TG_OP = 'UPDATE') THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address || ' ' || comment );
    RETURN NEW;
  END IF;
 END;
 $$;


ALTER FUNCTION public.bn_functions_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_functions_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_functions_comment_trigger() IS 'The bn_functions_comment_trigger is called for UPDATE operations on the bn_function table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to functions between multiple instances of BinNavi.';


--
-- Name: bn_functions_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_functions_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('function_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address );
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('function_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('function_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.module_id || ' ' || OLD.address );
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_functions_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_functions_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_functions_trigger() IS 'The bn_functions_trigger is called for all altering operations on the bn_functions table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi';


--
-- Name: bn_global_edge_comments_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_global_edge_comments_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
  IF ( TG_OP = 'INSERT' OR TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.src_module_id || ' ' || NEW.dst_module_id || ' ' || NEW.src_address || ' ' || NEW.dst_address || ' ' || NEW.comment_id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.src_module_id || ' ' || OLD.dst_module_id || ' ' || OLD.src_address || ' ' || OLD.dst_address);
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_global_edge_comments_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_global_edge_comments_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_global_edge_comments_trigger() IS 'The bn_global_edge_comments_trigger is called for all operations on the bn_global_edge_comments table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of global comments associated to edges between multiple instances of BinNavi.';


--
-- Name: bn_global_node_comments_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_global_node_comments_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
  IF ( TG_OP = 'INSERT' OR TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address || ' ' || NEW.comment_id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.module_id || ' ' || OLD.address);
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_global_node_comments_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_global_node_comments_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_global_node_comments_trigger() IS 'The bn_global_node_comments_trigger is called for all operations on the bn_global_node_comments table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of global comments associated to nodes between multiple instances of BinNavi.';


--
-- Name: bn_group_nodes_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_group_nodes_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  comment text;
BEGIN
  IF ( TG_OP = 'INSERT' OR TG_OP = 'UPDATE' ) THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.node_id || ' ' || comment );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.node_id);
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_group_nodes_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_group_nodes_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_group_nodes_comment_trigger() IS 'The bn_group_nodes_comment_trigger is called for all operations on the bn_group_nodes table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to group nodes between multiple instances of BinNavi.';


--
-- Name: bn_instructions_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_instructions_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  comment text;
BEGIN
  IF ( TG_OP = 'UPDATE') THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.address || ' ' || comment );
    RETURN NEW;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_instructions_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_instructions_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_instructions_comment_trigger() IS 'The bn_instructions_comment_trigger is called for UPDATE operations on the bn_instructions table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of global comments associated to instructions between multiple instances of BinNavi.';


--
-- Name: bn_module_views_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_module_views_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.view_id || ' ' || NEW.module_id );
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.view_id || ' ' || NEW.module_id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.view_id || ' ' || OLD.module_id );
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_module_views_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_module_views_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_module_views_trigger() IS 'The bn_module_views_trigger is called for all altering operations on the bn_module_views table and will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of module views between multiple instances of BinNavi.';


--
-- Name: bn_project_views_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_project_views_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.view_id || ' ' || NEW.project_id );
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.view_id || ' ' || NEW.project_id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.view_id || ' ' || OLD.project_id );
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_project_views_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_project_views_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_project_views_trigger() IS 'The bn_project_views_trigger is called for all altering operations on the bn_project_views table and will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of project views between multiple instances of BinNavi.';


--
-- Name: bn_sections_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_sections_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('section_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('section_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('section_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.module_id || ' ' || OLD.id );
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_sections_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_sections_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_sections_trigger() IS 'The bn_sections_trigger is called for all altering operations on the bn_sections table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi';


--
-- Name: bn_text_nodes_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_text_nodes_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  comment text;
BEGIN
  IF ( TG_OP = 'INSERT' OR TG_OP = 'UPDATE' ) THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.node_id || ' ' || comment );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.node_id);
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_text_nodes_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_text_nodes_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_text_nodes_comment_trigger() IS 'The bn_text_nodes_comment_trigger is called for all operations on the bn_text_nodes table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to text nodes between multiple instances of BinNavi.';


--
-- Name: bn_type_instances_comment_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_type_instances_comment_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  comment text;
BEGIN
  IF ( TG_OP = 'UPDATE') THEN
    IF ( NEW.comment_id is null) THEN
      comment = 'null';
    ELSE
      comment = CAST(NEW.comment_id AS TEXT);
    END IF;
    PERFORM pg_notify('comment_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id || ' ' || comment );
    RETURN NEW;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_type_instances_comment_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_type_instances_comment_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_type_instances_comment_trigger() IS 'The bn_type_instances_comment_trigger is called for UPDATE operations on the bn_type_instances table an will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to type instances between multiple instances of BinNavi.';


--
-- Name: bn_type_instances_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_type_instances_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('type_instances_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('type_instances_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('type_instances_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.module_id || ' ' || OLD.id );
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_type_instances_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_type_instances_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_type_instances_trigger() IS 'The bn_expression_type_instances_trigger is called for all altering operations on the bn_expression_type_instances table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi.';


--
-- Name: bn_types_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_types_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF ( TG_OP = 'INSERT' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.module_id || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('types_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.module_id || ' ' || OLD.id );
    RETURN OLD;
  END IF;
END;
$$;


ALTER FUNCTION public.bn_types_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_types_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_types_trigger() IS 'The bn_types_trigger is called for all altering operations on the bn_types table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi';


--
-- Name: bn_views_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION bn_views_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
 BEGIN
  IF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.id );
    RETURN OLD;
  END IF;
 END;
 $$;


ALTER FUNCTION public.bn_views_trigger() OWNER TO postgres;

--
-- Name: FUNCTION bn_views_trigger(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION bn_views_trigger() IS 'The bn_views_trigger is called for UPDATE and DELETE operations on the bn_views table and will perform a pg_notify with the altered information.
  This infromation is used in BinNavi to provide synchronisation of views between multiple instances of BinNavi.';


--
-- Name: colorize_module_nodes(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION colorize_module_nodes(moduleid integer) RETURNS void
    LANGUAGE sql
    AS $_$
  WITH entry_nodes AS (
    SELECT bn_nodes.id AS nodes_id FROM bn_nodes
      JOIN bn_module_views ON bn_nodes.view_id = bn_module_views.view_id
      AND bn_module_views.module_id = $1
      LEFT JOIN bn_edges ON bn_nodes.id = bn_edges.target_node_id
      WHERE target_node_id IS NULL
      AND bn_nodes.type = 'code'
    ), exit_nodes AS (
    SELECT bn_nodes.id AS nodes_id FROM bn_nodes
      JOIN bn_module_views ON bn_nodes.view_id = bn_module_views.view_id
      AND bn_module_views.module_id = $1
      LEFT JOIN bn_edges ON bn_nodes.id = bn_edges.source_node_id
      WHERE source_node_id IS NULL
      AND bn_nodes.type = 'code'
    ), single_nodes AS (
    SELECT nodes_id FROM entry_nodes INTERSECT SELECT nodes_id from exit_nodes
    ), update_entry_nodes AS (
    UPDATE bn_nodes SET bordercolor = -16736256 WHERE bn_nodes.id IN
      (SELECT * FROM entry_nodes)
    ), update_exit_nodes AS (
    UPDATE bn_nodes SET bordercolor = -6291456 WHERE bn_nodes.id IN
      (SELECT * FROM exit_nodes)
    )
  UPDATE bn_nodes SET bordercolor = -6250496 WHERE bn_nodes.id IN
    (SELECT * FROM single_nodes);
$_$;


ALTER FUNCTION public.colorize_module_nodes(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION colorize_module_nodes(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION colorize_module_nodes(moduleid integer) IS 'This function creates the initial colors for nodes of a module that is getting converted.';


--
-- Name: connect_instructions_to_code_nodes(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION connect_instructions_to_code_nodes(rawmoduleid integer, moduleid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    minnodeid int;
  BEGIN
    SELECT MIN(node_id) INTO minnodeid
      FROM bn_code_nodes
     WHERE module_id = moduleid;

  EXECUTE 'INSERT INTO bn_codenode_instructions
             (module_id, node_id, position, address, comment_id)
             SELECT '|| moduleid ||', basic_block_id + '|| minnodeid ||' - 1 AS nodeId,
               bbi.sequence, bbi.instruction, null
             FROM ex_'|| rawmoduleid ||'_basic_block_instructions AS bbi';

  END;
$$;


ALTER FUNCTION public.connect_instructions_to_code_nodes(rawmoduleid integer, moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION connect_instructions_to_code_nodes(rawmoduleid integer, moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION connect_instructions_to_code_nodes(rawmoduleid integer, moduleid integer) IS 'This function connects the instruction to code nodes in the conversion from a raw module to a BinNavi module.';


--
-- Name: create_expression_type_instance(integer, bigint, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_expression_type_instance(moduleid integer, operandaddress bigint, operandposition integer, expressionid integer, typeinstanceid integer) RETURNS void
    LANGUAGE sql
    AS $_$

    INSERT INTO bn_expression_type_instances (module_id, address, position, expression_id, type_instance_id)
        VALUES ($1, $2, $3, $4, $5);

$_$;


ALTER FUNCTION public.create_expression_type_instance(moduleid integer, operandaddress bigint, operandposition integer, expressionid integer, typeinstanceid integer) OWNER TO postgres;

--
-- Name: FUNCTION create_expression_type_instance(moduleid integer, operandaddress bigint, operandposition integer, expressionid integer, typeinstanceid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_expression_type_instance(moduleid integer, operandaddress bigint, operandposition integer, expressionid integer, typeinstanceid integer) IS 'This function creates a new expression type instance which connects a type instance in a section to an operand in the graph view.';


--
-- Name: create_module(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_module(rawmoduleid integer) RETURNS integer
    LANGUAGE sql
    AS $_$
INSERT INTO bn_modules
  (name, raw_module_id, md5, sha1, description, file_base, image_base, import_time)
  (SELECT name, id, md5, sha1, comment, base_address, base_address, NOW()
  FROM modules WHERE id = $1) RETURNING id
$_$;


ALTER FUNCTION public.create_module(rawmoduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION create_module(rawmoduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_module(rawmoduleid integer) IS 'This function creates the entry in the BinNavi modules table in a raw module to BinNavi module conversion.';


--
-- Name: create_native_call_graph_view(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_native_call_graph_view(moduleid integer) RETURNS integer
    LANGUAGE sql
    AS $_$

WITH callgraphid AS (
  INSERT INTO bn_views
    (type, name, description, creation_date, modification_date)
    VALUES('native', 'Native Callgraph', null, NOW(), NOW())
    RETURNING id
)
INSERT INTO bn_module_views (view_id, module_id)
  SELECT id, $1 FROM callgraphid
  RETURNING view_id;

$_$;


ALTER FUNCTION public.create_native_call_graph_view(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION create_native_call_graph_view(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_native_call_graph_view(moduleid integer) IS 'This function creates the native call graph information and updates the tables accordingly.';


--
-- Name: create_native_callgraph_edges(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_native_callgraph_edges(rawmoduleid integer, moduleid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
  BEGIN
    EXECUTE 'DROP INDEX IF EXISTS ex_'|| rawmoduleid ||'_callgraph_source_idx';
    EXECUTE 'DROP INDEX IF EXISTS ex_'|| rawmoduleid ||'_callgraph_destination_idx';
    EXECUTE 'CREATE INDEX ex_'|| rawmoduleid ||'_callgraph_source_idx
             ON ex_'|| rawmoduleid ||'_callgraph USING btree(source)';
    EXECUTE 'CREATE INDEX ex_'|| rawmoduleid ||'_callgraph_destination_idx
             ON ex_'|| rawmoduleid ||'_callgraph USING btree(destination)';

    EXECUTE 'INSERT INTO bn_edges
      (source_node_id, target_node_id, x1, y1, x2, y2, type, color, selected, visible, comment_id)
      SELECT source_function.node_id, destination_function.node_id, 0, 0, 0, 0, ''jump_unconditional'', 0, false, true, null
        FROM ex_'|| rawmoduleid ||'_callgraph AS callgraph
      INNER JOIN bn_function_nodes AS source_function ON source_function.module_id = '|| moduleid ||'
        AND source_function.function = callgraph.source
      INNER JOIN bn_function_nodes as destination_function ON destination_function.module_id = '|| moduleid ||'
        AND destination_function.function = callgraph.destination';

  END;
$$;


ALTER FUNCTION public.create_native_callgraph_edges(rawmoduleid integer, moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION create_native_callgraph_edges(rawmoduleid integer, moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_native_callgraph_edges(rawmoduleid integer, moduleid integer) IS 'This function creates the edges for the native call graph of a module.';


--
-- Name: create_native_callgraph_nodes(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_native_callgraph_nodes(viewid integer, moduleid integer) RETURNS void
    LANGUAGE sql
    AS $_$
WITH allinfo AS (
  SELECT nextval('bn_nodes_id_seq'::regclass) as id ,$1 as view_id, address, $2 as module_id
    FROM bn_functions WHERE module_id = $2
), ni AS (
  INSERT INTO bn_nodes
    (id, view_id, parent_id, type, x, y, width, height, color, selected, visible)
    SELECT id, view_id, null, 'function', 0, 0, 0, 0, 0, false, true FROM allinfo
)
INSERT INTO bn_function_nodes
  (node_id, module_id, function, comment_id)
  SELECT id, module_id, address, null FROM allinfo
$_$;


ALTER FUNCTION public.create_native_callgraph_nodes(viewid integer, moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION create_native_callgraph_nodes(viewid integer, moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_native_callgraph_nodes(viewid integer, moduleid integer) IS 'This function creates the function nodes for the native call graph in a raw module to BinNavi module conversion.';


--
-- Name: create_native_code_nodes(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_native_code_nodes(rawmoduleid integer, moduleid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
  DECLARE
    minnodeid int;
  BEGIN
    EXECUTE 'DROP INDEX IF EXISTS ex_'|| rawmoduleid ||'_basic_blocks_parent_function_idx';
    EXECUTE 'CREATE INDEX ex_'|| rawmoduleid ||'_basic_blocks_parent_function_idx
             ON ex_'|| rawmoduleid ||'_basic_blocks USING btree(parent_function)';

    EXECUTE 'INSERT INTO bn_nodes
            (view_id, parent_id, type, x, y, width, height, color, selected, visible)
            SELECT view_id, null, ''code'', 0, 0, 0, 0, 0, false, true
              FROM ex_'|| rawmoduleid ||'_basic_blocks as basic_blocks
            INNER JOIN bn_functions AS functions ON functions.module_id = '|| moduleid ||'
              AND functions.address = basic_blocks.parent_function
              AND functions.type != ''import''
            INNER JOIN bn_function_views AS fvt ON fvt.module_id = '|| moduleid ||'
              AND fvt.function = basic_blocks.parent_function
              ORDER BY id
            RETURNING id' INTO minnodeid;

     INSERT INTO bn_code_nodes SELECT moduleid, nt.id, function, null
     FROM bn_function_views AS fvt
       INNER JOIN bn_nodes AS nt ON nt.view_id = fvt.view_id AND nt.type = 'code'
         AND nt.id >= minnodeid WHERE fvt.module_id = moduleid;

  END;
$$;


ALTER FUNCTION public.create_native_code_nodes(rawmoduleid integer, moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION create_native_code_nodes(rawmoduleid integer, moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_native_code_nodes(rawmoduleid integer, moduleid integer) IS 'This function creates the code nodes for all native flow graphs in a raw module to BinNavi module conversion.';


--
-- Name: create_native_flowgraph_edges(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_native_flowgraph_edges(rawmoduleid integer, moduleid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
  DECLARE
    minnodeid int;
  BEGIN
    SELECT MIN(node_id) INTO minnodeid
      FROM bn_code_nodes
     WHERE module_id = moduleid;

  EXECUTE 'INSERT INTO bn_edges
  (source_node_id, target_node_id, x1, y1, x2, y2, type, color, visible, selected, comment_id)
  SELECT fg.source + '|| minnodeid ||' - 1 AS source,
         fg.destination + '|| minnodeid ||' - 1 AS target, 0, 0, 0, 0,
  (ENUM_RANGE(NULL::edge_type))[fg.type + 1], 0, true, false, null
  FROM ex_'|| rawmoduleid ||'_control_flow_graphs AS fg';

  END;
$$;


ALTER FUNCTION public.create_native_flowgraph_edges(rawmoduleid integer, moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION create_native_flowgraph_edges(rawmoduleid integer, moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_native_flowgraph_edges(rawmoduleid integer, moduleid integer) IS 'This function creates the edges in the native flowgraphs during a rawmodule to module converstion.';


--
-- Name: create_native_flowgraph_views(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_native_flowgraph_views(moduleid integer) RETURNS void
    LANGUAGE sql
    AS $_$

WITH allinfo AS (
  SELECT nextval('bn_views_id_seq') AS id, address, COALESCE(name, original_name) AS name
  FROM bn_functions WHERE module_id = $1
), views AS (
  INSERT INTO bn_views(id, type, name, description)
  SELECT id, 'native', name, null FROM allinfo
), function_views AS (
  INSERT INTO bn_function_views
  SELECT $1, id, address FROM allinfo
)
INSERT INTO bn_module_views (view_id, module_id)
  SELECT id, $1 FROM allinfo

$_$;


ALTER FUNCTION public.create_native_flowgraph_views(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION create_native_flowgraph_views(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_native_flowgraph_views(moduleid integer) IS 'This function creates the native flowgraph information in the database during a conversion and updates the related tables accordingly.';


--
-- Name: create_section(integer, text, integer, bigint, bigint, permission_type, bytea); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_section(moduleid integer, name text, comment_id integer, start_address bigint, end_address bigint, permission permission_type, data bytea) RETURNS integer
    LANGUAGE sql
    AS $_$

    INSERT INTO bn_sections (module_id, name, comment_id, start_address, end_address, permission, data)
         VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id;

$_$;


ALTER FUNCTION public.create_section(moduleid integer, name text, comment_id integer, start_address bigint, end_address bigint, permission permission_type, data bytea) OWNER TO postgres;

--
-- Name: FUNCTION create_section(moduleid integer, name text, comment_id integer, start_address bigint, end_address bigint, permission permission_type, data bytea); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_section(moduleid integer, name text, comment_id integer, start_address bigint, end_address bigint, permission permission_type, data bytea) IS 'This function creates a new section and returns its id';


--
-- Name: create_type_instance(integer, text, integer, integer, integer, bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_type_instance(moduleid integer, instancename text, commentid integer, typeid integer, sectionid integer, instanceaddress bigint) RETURNS integer
    LANGUAGE sql
    AS $_$

    INSERT INTO bn_type_instances (module_id, name, comment_id, type_id, section_id, section_offset)
        VALUES ($1, $2, $3, $4, $5, $6) RETURNING id;

$_$;


ALTER FUNCTION public.create_type_instance(moduleid integer, instancename text, commentid integer, typeid integer, sectionid integer, instanceaddress bigint) OWNER TO postgres;

--
-- Name: FUNCTION create_type_instance(moduleid integer, instancename text, commentid integer, typeid integer, sectionid integer, instanceaddress bigint); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_type_instance(moduleid integer, instancename text, commentid integer, typeid integer, sectionid integer, instanceaddress bigint) IS 'This function creates a new type instance and returns the generated id of it.';


--
-- Name: create_type_substitution(integer, bigint, integer, integer, integer, integer[], integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION create_type_substitution(moduleid integer, address bigint, "position" integer, expressionid integer, basetypeid integer, path integer[], "offset" integer) RETURNS void
    LANGUAGE sql
    AS $_$
INSERT INTO bn_expression_types (module_id, address, position, expression_id, base_type_id, path, offset)
  VALUES($1, $2, $3, $4, $5, $6, $7);
$_$;


ALTER FUNCTION public.create_type_substitution(moduleid integer, address bigint, "position" integer, expressionid integer, basetypeid integer, path integer[], "offset" integer) OWNER TO postgres;

--
-- Name: FUNCTION create_type_substitution(moduleid integer, address bigint, "position" integer, expressionid integer, basetypeid integer, path integer[], "offset" integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION create_type_substitution(moduleid integer, address bigint, "position" integer, expressionid integer, basetypeid integer, path integer[], "offset" integer) IS 'This function creates a single type substitution';


--
-- Name: delete_comment_by_id(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_comment_by_id(commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
    DECLARE
	  parentid integer;
    BEGIN
   WITH parent_comment AS (
   SELECT one.id AS id, one.parent_id AS parent FROM bn_comments AS one
    JOIN bn_comments AS two
      ON one.parent_id = two.id
   WHERE one.id = commentid
   ),
   child_comment AS (
   SELECT one.id AS id, two.id AS child FROM bn_comments AS one
    JOIN bn_comments AS two
      ON one.id = two.parent_id
   WHERE one.id = commentid
   ),

   update AS (UPDATE bn_comments SET parent_id =
	(
	SELECT parent FROM parent_comment
	  LEFT JOIN child_comment
	    ON child_comment.id = parent_comment.id
	) WHERE id =
	(
	SELECT child FROM parent_comment
	  LEFT JOIN child_comment
	    ON child_comment.id = parent_comment.id
	)
	AND user_id = userid
	)

    DELETE FROM bn_comments
    WHERE bn_comments.id = commentid
      AND bn_comments.user_id = userid
    RETURNING bn_comments.parent_id INTO STRICT parentid;

    RETURN parentid;

    EXCEPTION
      WHEN NO_DATA_FOUND THEN
      RAISE EXCEPTION 'the comment with id % does not exist or is not owned by the user with id %', commentId, userId;

END;
 $$;


ALTER FUNCTION public.delete_comment_by_id(commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_comment_by_id(commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_comment_by_id(commentid integer, userid integer) IS 'This function deletes a comment by id. It is used by all other comment delete functions as core functionality.
 It does verify if the user id given as argument is the actual owner of the comment which is requested to be deleted and will fail otherwise.';


--
-- Name: delete_expression_type_instance(integer, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_expression_type_instance(moduleid integer, address bigint, "position" integer, expressionid integer) RETURNS void
    LANGUAGE sql
    AS $_$

    DELETE
     FROM bn_expression_type_instances
     WHERE module_id = $1
       AND address = $2
       AND "position" = $3
       AND expression_id = $4;

$_$;


ALTER FUNCTION public.delete_expression_type_instance(moduleid integer, address bigint, "position" integer, expressionid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_expression_type_instance(moduleid integer, address bigint, "position" integer, expressionid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_expression_type_instance(moduleid integer, address bigint, "position" integer, expressionid integer) IS 'This function deletes a expression type instance (cross reference).';


--
-- Name: delete_function_comment(integer, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_function_comment(moduleid integer, functionaddress bigint, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the function nodes table with the newly generated comment.
    --
    UPDATE bn_functions
      SET comment_id = parentid
    WHERE module_id = moduleid
      AND address = functionaddress
      AND comment_id = commentId;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_function_comment(moduleid integer, functionaddress bigint, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_function_comment(moduleid integer, functionaddress bigint, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_function_comment(moduleid integer, functionaddress bigint, commentid integer, userid integer) IS 'This function deletes a comment associated to a function. It does return the id of the deleted comment.';


--
-- Name: delete_function_node_comment(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_function_node_comment(moduleid integer, nodeid integer, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the function nodes table with the newly generated comment.
    --
    UPDATE bn_function_nodes
      SET comment_id = parentid
    WHERE node_id = nodeid
      AND module_id = moduleid
      AND comment_id = commentId;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_function_node_comment(moduleid integer, nodeid integer, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_function_node_comment(moduleid integer, nodeid integer, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_function_node_comment(moduleid integer, nodeid integer, commentid integer, userid integer) IS 'This function deletes a comment associated to a function node. It returns the id of the deleted comment.';


--
-- Name: delete_global_code_node_comment(integer, integer, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_global_code_node_comment(moduleid integer, node_id integer, node_address bigint, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the global comments according to the changes.
    --
    IF parentid IS NULL THEN
      DELETE FROM bn_global_node_comments
        WHERE module_id = moduleid
          AND address = node_address
          AND comment_id = commentId;
    ELSE
	UPDATE bn_global_node_comments
	   SET comment_id = parentid
	 WHERE module_id = moduleId
	   AND address = node_address
	   AND comment_id = commentId;
    END IF;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_global_code_node_comment(moduleid integer, node_id integer, node_address bigint, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_global_code_node_comment(moduleid integer, node_id integer, node_address bigint, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_global_code_node_comment(moduleid integer, node_id integer, node_address bigint, commentid integer, userid integer) IS 'This function deletes a global comment assocatiated with a code node. It returns the id of the deleted comment.';


--
-- Name: delete_global_edge_comment(integer, integer, bigint, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_global_edge_comment(srcmoduleid integer, dstmoduleid integer, srcnodeaddress bigint, dstnodeaddress bigint, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the global comments according to the changes.
    --
    IF parentid IS NULL THEN
      DELETE FROM bn_global_edge_comments
        WHERE src_module_id = srcmoduleid
          AND dst_module_id = dstmoduleid
          AND src_address = srcnodeaddress
          AND dst_address = dstnodeaddress
          AND comment_id = commentId;
    ELSE
      UPDATE bn_global_edge_comments
         SET comment_id = parentid
       WHERE dst_module_id = dstmoduleid
         AND src_address = srcnodeaddress
         AND dst_address = dstnodeaddress
         AND comment_id = commentId;
    END IF;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_global_edge_comment(srcmoduleid integer, dstmoduleid integer, srcnodeaddress bigint, dstnodeaddress bigint, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_global_edge_comment(srcmoduleid integer, dstmoduleid integer, srcnodeaddress bigint, dstnodeaddress bigint, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_global_edge_comment(srcmoduleid integer, dstmoduleid integer, srcnodeaddress bigint, dstnodeaddress bigint, commentid integer, userid integer) IS 'This function deletes a global comment associated with an edge. It does return the id of the deleted comment.';


--
-- Name: delete_global_instruction_comment(integer, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_global_instruction_comment(moduleid integer, instruction_address bigint, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the record of the instruction to point to the
    -- new comment.
    --
    UPDATE bn_instructions
      SET comment_id = parentid
      WHERE module_id = moduleid
        AND address = instruction_address
        AND comment_id = commentId;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_global_instruction_comment(moduleid integer, instruction_address bigint, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_global_instruction_comment(moduleid integer, instruction_address bigint, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_global_instruction_comment(moduleid integer, instruction_address bigint, commentid integer, userid integer) IS 'This function deletes a global comment associated with an instruction. It returns the id of the deleted comment.';


--
-- Name: delete_group_node_comment(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_group_node_comment(nodeid integer, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the group nodes table with the newly generated comment.
    --
    UPDATE bn_group_nodes
      SET comment_id = parentid
    WHERE node_id = nodeId
      AND comment_id = commentid;

    RETURN commentid;

  END;
$$;


ALTER FUNCTION public.delete_group_node_comment(nodeid integer, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_group_node_comment(nodeid integer, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_group_node_comment(nodeid integer, commentid integer, userid integer) IS 'This function deletes a comment from a group node. It returns the id of the deleted comment.';


--
-- Name: delete_local_code_node_comment(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_local_code_node_comment(moduleid integer, nodeid integer, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the code node with the newly generated comment.
    --
    UPDATE bn_code_nodes
       SET comment_id = parentid
     WHERE module_id = moduleid
       AND node_id = nodeid
       AND comment_id = commentId;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_local_code_node_comment(moduleid integer, nodeid integer, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_local_code_node_comment(moduleid integer, nodeid integer, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_local_code_node_comment(moduleid integer, nodeid integer, commentid integer, userid integer) IS 'This funciton deletes a local comment associated with a code node. It returns the id of the deleted comment.';


--
-- Name: delete_local_edge_comment(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_local_edge_comment(edge_id integer, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the edge with the newly generated comment.
    --
    UPDATE bn_edges
      SET comment_id = parentid
    WHERE id = edge_id
      AND comment_id = commentId;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_local_edge_comment(edge_id integer, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_local_edge_comment(edge_id integer, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_local_edge_comment(edge_id integer, commentid integer, userid integer) IS 'This function deletes a local comment associated with an edge. It returns the id of the deleted comment.';


--
-- Name: delete_local_instruction_comment(integer, integer, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_local_instruction_comment(moduleid integer, nodeid integer, instruction_address bigint, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the record of the instruction to point to the
    -- new comment.
    --
    UPDATE bn_codenode_instructions
      SET comment_id = parentid
      WHERE module_id = moduleid
        AND node_id = nodeid
        AND address = instruction_address
        AND comment_id = commentId;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_local_instruction_comment(moduleid integer, nodeid integer, instruction_address bigint, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_local_instruction_comment(moduleid integer, nodeid integer, instruction_address bigint, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_local_instruction_comment(moduleid integer, nodeid integer, instruction_address bigint, commentid integer, userid integer) IS 'This function deletes a local comment associated with an instruction in a code node. It returns the id of the deleted comment.';


--
-- Name: delete_section(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_section(moduleid integer, sectionid integer) RETURNS void
    LANGUAGE sql
    AS $_$

	DELETE
	 FROM bn_sections
	 WHERE module_id = $1
	 AND id = $2;

$_$;


ALTER FUNCTION public.delete_section(moduleid integer, sectionid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_section(moduleid integer, sectionid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_section(moduleid integer, sectionid integer) IS 'This function deletes a section.';


--
-- Name: delete_section_comment(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_section_comment(moduleid integer, sectionid integer, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the function nodes table with the newly generated comment.
    --
    UPDATE bn_sections
      SET comment_id = parentid
    WHERE module_id = moduleid
      AND id = sectionid
      AND comment_id = commentId;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_section_comment(moduleid integer, sectionid integer, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_section_comment(moduleid integer, sectionid integer, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_section_comment(moduleid integer, sectionid integer, commentid integer, userid integer) IS 'This function deletes a comment associated to a section. It does return the id of the deleted comment.';


--
-- Name: delete_text_node_comment(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_text_node_comment(nodeid integer, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the text nodes table with the newly generated comment.
    --
    UPDATE bn_text_nodes
      SET comment_id = parentid
    WHERE node_id = nodeId
      AND comment_id = commentid;

    RETURN commentid;

  END;
$$;


ALTER FUNCTION public.delete_text_node_comment(nodeid integer, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_text_node_comment(nodeid integer, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_text_node_comment(nodeid integer, commentid integer, userid integer) IS 'This function deletes a comment from a text node. It returns the id of the deleted comment.';


--
-- Name: delete_type(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_type(module_id integer, type_id integer) RETURNS void
    LANGUAGE sql
    AS $_$
DELETE FROM bn_types AS t
  WHERE t.module_id = $1
    AND t.id = $2;
$_$;


ALTER FUNCTION public.delete_type(module_id integer, type_id integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_type(module_id integer, type_id integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_type(module_id integer, type_id integer) IS 'This function deletes a single type from bn_types also known as a member type if leaves a hole in the compound type at the member offset as large as the members size.';


--
-- Name: delete_type_compact(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_type_compact(module_id integer, type_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  deleted bn_types;
  deletedsize integer;
BEGIN
  --
  -- Delete the type first.
  --
  SELECT delete_type(module_id, type_id)
    INTO deleted;

  --
  -- Find the size of the type.
  --
  SELECT size FROM bn_base_types bt
    WHERE bt.module_id = module_id
	  AND bt.id = deleted.base_type
	 INTO deletedsize;

  --
  -- Then update the offsets.
  --
  PERFORM update_type_offsets(module_id, deleted.parent_id, deletedsize, deleted.offset);
END;
$$;


ALTER FUNCTION public.delete_type_compact(module_id integer, type_id integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_type_compact(module_id integer, type_id integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_type_compact(module_id integer, type_id integer) IS 'This function deletes a single type from bn_types and compacts the other elements in the compound type to leave no hole where the element has been.';


--
-- Name: delete_type_instance(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_type_instance(moduleid integer, typeinstanceid integer) RETURNS void
    LANGUAGE sql
    AS $_$

	DELETE
     FROM bn_type_instances
     WHERE module_id = $1
     AND id = $2;

$_$;


ALTER FUNCTION public.delete_type_instance(moduleid integer, typeinstanceid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_type_instance(moduleid integer, typeinstanceid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_type_instance(moduleid integer, typeinstanceid integer) IS 'This function delete a type instance.';


--
-- Name: delete_type_instance_comment(integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_type_instance_comment(moduleid integer, typeinstanceid integer, commentid integer, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  DECLARE
    parentid integer;
  BEGIN
    --
    -- Delete the comment according to the specified user and
    -- comment ids.
    --
    SELECT delete_comment_by_id(commentId, userId)
      INTO parentid;

    --
    -- Update the function nodes table with the newly generated comment.
    --
    UPDATE bn_type_instances
      SET comment_id = parentid
    WHERE module_id = moduleid
      AND id = typeinstanceid
      AND comment_id = commentId;

    RETURN commentId;
  END;
$$;


ALTER FUNCTION public.delete_type_instance_comment(moduleid integer, typeinstanceid integer, commentid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_type_instance_comment(moduleid integer, typeinstanceid integer, commentid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_type_instance_comment(moduleid integer, typeinstanceid integer, commentid integer, userid integer) IS 'This function deletes a comment associated to a type instance. It does return the id of the deleted comment.';


--
-- Name: delete_type_substitution(integer, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION delete_type_substitution(moduleid integer, address bigint, "position" integer, expressionid integer) RETURNS void
    LANGUAGE sql
    AS $_$
DELETE FROM bn_expression_types AS et
  WHERE et.module_id = $1
    AND et.address = $2
	AND et.position = $3
	AND et.expression_id = $4;
$_$;


ALTER FUNCTION public.delete_type_substitution(moduleid integer, address bigint, "position" integer, expressionid integer) OWNER TO postgres;

--
-- Name: FUNCTION delete_type_substitution(moduleid integer, address bigint, "position" integer, expressionid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION delete_type_substitution(moduleid integer, address bigint, "position" integer, expressionid integer) IS 'This function deletes a single type substitution';


--
-- Name: edit_comment(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION edit_comment(comment_id integer, userid integer, commenttext text) RETURNS void
    LANGUAGE plpgsql
    AS $$
  DECLARE
    result integer;
  BEGIN
    --
    -- Find out if there is a comment record which is owned
    -- by the user id given as argument.
    --
    SELECT c.id INTO STRICT result FROM bn_comments AS c
      WHERE c.id = comment_id
        AND c.user_id = userid;

    --
    -- If the record exists proceed with the update.
    --
    IF FOUND THEN
      UPDATE bn_comments
        SET comment_text = commenttext
        WHERE id = comment_id
          AND user_id = userid;
    END IF;

    --
    -- If either the comment is not owned by the supplied user id
    -- or if there are actually more comments which come up for the same
    -- key we bail out and inform the caller.
    --
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        RAISE EXCEPTION 'Error: there is no comment record in the database which satisfies id = % user = %.', comment_id, userid;
      WHEN TOO_MANY_ROWS THEN
        RAISE EXCEPTION 'Error: there is more then one comment which satisfies id = % user = %.', comment_id, userid;
  END;
$$;


ALTER FUNCTION public.edit_comment(comment_id integer, userid integer, commenttext text) OWNER TO postgres;

--
-- Name: FUNCTION edit_comment(comment_id integer, userid integer, commenttext text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION edit_comment(comment_id integer, userid integer, commenttext text) IS 'This function edits a comment based on the id and the comment text provided.
  It checks if the user given is the owner of the comment and will fail if this is not the case.
  This function is used for all edit functions.';


--
-- Name: get_all_comment_ancestors(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION get_all_comment_ancestors(comment_id integer) RETURNS TABLE(level integer, id integer, parent_id integer, user_id integer, comment text)
    LANGUAGE sql
    AS $_$
  WITH RECURSIVE
        q AS
        (
        SELECT  h.*, 1 AS level
        FROM    bn_comments h
        WHERE   id = $1
        UNION ALL
        SELECT  hp.*, level + 1
        FROM    q
        JOIN    bn_comments hp
        ON      hp.id = q.parent_id
        )
SELECT  level, id, parent_id, user_id, comment_text
FROM    q
ORDER BY level DESC
 $_$;


ALTER FUNCTION public.get_all_comment_ancestors(comment_id integer) OWNER TO postgres;

--
-- Name: FUNCTION get_all_comment_ancestors(comment_id integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION get_all_comment_ancestors(comment_id integer) IS 'This function gets all ancestors of a comment for the provided comment id.';


--
-- Name: get_all_comment_ancestors_multiple(integer[]); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION get_all_comment_ancestors_multiple(commentids integer[]) RETURNS TABLE(commentid integer, level integer, id integer, parent_id integer, user_id integer, comment text)
    LANGUAGE plpgsql
    AS $_$
  DECLARE
    commentId integer;
  BEGIN
    FOREACH commentId IN ARRAY $1
    LOOP
      RETURN QUERY(
        SELECT commentId, * FROM get_all_comment_ancestors(commentId)
      );
    END LOOP;
  END;
$_$;


ALTER FUNCTION public.get_all_comment_ancestors_multiple(commentids integer[]) OWNER TO postgres;

--
-- Name: FUNCTION get_all_comment_ancestors_multiple(commentids integer[]); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION get_all_comment_ancestors_multiple(commentids integer[]) IS 'This function gets all ancestors of all comment ids provided as argument.
  It can be used as to batch load comments and reduce the number of querries needed to do so.';


--
-- Name: get_derived_views(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION get_derived_views(viewid integer) RETURNS integer
    LANGUAGE sql
    AS $_$

SELECT vt.id FROM bn_codenode_instructions AS cit
  JOIN (
    SELECT cit.address FROM bn_views AS vt
      JOIN bn_nodes AS nt ON vt.id = nt.view_id
      JOIN bn_codenode_instructions AS cit ON nt.id = cit.node_id
    WHERE vt.id = $1
  ) AS a ON cit.address = a.address
  JOIN bn_nodes AS nt ON cit.node_id = nt.id
  JOIN bn_views AS vt ON vt.id = nt.view_id
  WHERE vt.type = 'non-native' GROUP by vt.id;

$_$;


ALTER FUNCTION public.get_derived_views(viewid integer) OWNER TO postgres;

--
-- Name: FUNCTION get_derived_views(viewid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION get_derived_views(viewid integer) IS 'This function retrieves the views from the database which have been derived from the given view.';


--
-- Name: get_sections(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION get_sections(moduleid integer) RETURNS TABLE(module_id integer, id integer, name text, comment_id integer, start_address bigint, end_address bigint, permission permission_type, data bytea)
    LANGUAGE sql
    AS $_$

    SELECT *
      FROM bn_sections
     WHERE module_id = $1;

$_$;


ALTER FUNCTION public.get_sections(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION get_sections(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION get_sections(moduleid integer) IS 'This function returns all sections associated with the given module id.';


--
-- Name: import(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION import(rawmoduleid integer, moduleid integer, userid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    arch text;
    viewid int;
  BEGIN

  --
  -- Disable all trigger procedures during the import.
  --

  SET session_replication_role = replica;

  SELECT architecture INTO arch FROM modules WHERE id = rawmoduleid;

  --
  -- import expression tree ids.
  --

  EXECUTE 'INSERT INTO bn_expression_tree_ids
           SELECT '|| moduleid ||', id
           FROM ex_'|| rawmoduleid ||'_expression_trees';

  --
  -- import expression trees.
  --

  EXECUTE 'INSERT INTO bn_expression_tree
          (module_id, id, type, symbol, immediate, position, parent_id)
          SELECT '|| moduleid ||', id, type, symbol, immediate, position, parent_id
          FROM ex_'|| rawmoduleid ||'_expression_nodes';

  --
  -- Connect expression trees.
  --

  EXECUTE 'INSERT INTO bn_expression_tree_mapping
          (SELECT '|| moduleid ||', expression_tree_id, expression_node_id
          FROM ex_'|| rawmoduleid ||'_expression_tree_nodes)';

  --
  -- import instructions.
  --

  EXECUTE 'WITH comments_to_id(id, address, comment) AS (
            SELECT nextval(''bn_comments_id_seq''::regclass), address, comment
            FROM ex_'|| rawmoduleid ||'_address_comments
          ), comments_table AS (
            INSERT INTO bn_comments
              (id, parent_id, user_id, comment_text)
              SELECT id, null, '|| userid ||', comment
                FROM comments_to_id
           )
           INSERT INTO bn_instructions
             (module_id, address, mnemonic, data, native, architecture, comment_id)
             SELECT '|| moduleid ||', isn.address, mnemonic, data, true, '|| quote_literal(arch) ||', com.id
             FROM ex_'|| rawmoduleid ||'_instructions AS isn
               LEFT JOIN comments_to_id AS com ON com.address = isn.address';

  --
  -- import operands.
  --

  EXECUTE 'INSERT INTO bn_operands SELECT '|| moduleid ||', address, expression_tree_id, position
          FROM ex_'|| rawmoduleid ||'_operands';

  --
  -- import expression substitutions.
  --

  EXECUTE 'INSERT INTO bn_expression_substitutions
           (module_id, address, position, expression_id, replacement)
           SELECT '|| moduleid ||', address, position, expression_node_id, replacement
           FROM ex_'|| rawmoduleid ||'_expression_substitutions';

  --
  -- Import address references.
  --

  EXECUTE 'INSERT INTO bn_address_references
           (module_id, address, position, expression_id, type, target)
           SELECT '|| moduleid ||', address, position, expression_node_id,
           (ENUM_RANGE(NULL::address_reference_type))[type + 1], destination
           FROM ex_'|| rawmoduleid ||'_address_references
             WHERE position IS NOT NULL AND expression_node_id IS NOT NULL';

  --
  -- Import functions.
  --

  EXECUTE 'INSERT INTO bn_functions
           (module_id, address, name, original_name, type, description,
           parent_module_name, parent_module_id, parent_module_function, comment_id, stack_frame)
           SELECT '|| moduleid ||', address, demangled_name, name, (ENUM_RANGE(NULL::function_type))[type + 1],
           null, module_name, null, null, null, stack_frame
           FROM ex_'|| rawmoduleid ||'_functions';

  --
  -- import base types.
  --

  EXECUTE 'INSERT INTO bn_base_types
           SELECT '|| moduleid ||', id, name, size, pointer, signed, category::text::type_category
           FROM ex_'|| rawmoduleid ||'_base_types';
  EXECUTE 'SELECT setval(''bn_base_types_id_seq'', COALESCE((SELECT MAX(id) + 1 FROM bn_base_types), 1), false)
           FROM bn_base_types';

  --
  -- import types.
  --

  EXECUTE 'INSERT INTO bn_types
           SELECT '|| moduleid ||', raw_types.id, raw_types.name, raw_types.base_type, raw_types.parent_id,
           raw_types.offset, raw_types.argument, raw_types.number_of_elements
           FROM ex_'|| rawmoduleid ||'_types AS raw_types';
  EXECUTE 'SELECT setval(''bn_types_id_seq'', COALESCE((SELECT MAX(id) + 1 FROM bn_types), 1), false)
           FROM bn_types';

  --
  -- import expression types.
  --

  EXECUTE 'INSERT INTO bn_expression_types
           SELECT '|| moduleid ||', et.address, et.position, et.expression_id, et.type, et.path, et.offset
           FROM ex_'|| rawmoduleid ||'_expression_types AS et';

  --
  -- import sections
  --

  EXECUTE 'INSERT INTO bn_sections (module_id, id, name, comment_id, start_address, end_address, permission, data)
           SELECT '|| moduleid ||', id, name, NULL, start_address, end_address, permission::text::permission_type, data
		   FROM ex_'|| rawmoduleid ||'_sections';
  EXECUTE 'SELECT setval(''bn_sections_id_seq'', COALESCE((SELECT MAX(id) + 1 FROM bn_sections), 1), false)
           FROM bn_sections';

  --
  -- import type instances
  --

  EXECUTE 'INSERT INTO bn_type_instances (module_id, id, name, type_id, section_id, section_offset)
           SELECT '|| moduleid ||', id, name, type_id, section_id, section_offset
           FROM ex_'|| rawmoduleid ||'_type_instances';
  EXECUTE 'SELECT setval(''bn_type_instances_id_seq'', COALESCE((SELECT MAX(id) + 1 FROM bn_type_instances), 1), false)
           FROM bn_type_instances';

  --
  -- import expression type instances
  --

  EXECUTE 'INSERT INTO bn_expression_type_instances
           SELECT '|| moduleid ||', address, position, expression_node_id, type_instance_id
           FROM ex_'|| rawmoduleid ||'_expression_type_instances';

  PERFORM create_native_flowgraph_views(moduleid);
  PERFORM create_native_code_nodes(rawmoduleid, moduleid);
  PERFORM connect_instructions_to_code_nodes(rawmoduleid, moduleid);
  PERFORM create_native_flowgraph_edges(rawmoduleid,moduleid);
  PERFORM colorize_module_nodes(moduleid);
  SELECT * INTO viewid FROM create_native_call_graph_view(moduleid);
  PERFORM create_native_callgraph_nodes(viewid, moduleid);
  PERFORM create_native_callgraph_edges(rawmoduleid, moduleid);

  UPDATE bn_modules SET initialization_state = 2147483647 WHERE id = moduleid;

  --
  -- Enable all trigger procedures after the import.
  --

  SET session_replication_role = DEFAULT;

  ANALYZE bn_nodes;
  ANALYZE bn_code_nodes;
  ANALYZE bn_function_nodes;
  ANALYZE bn_edges;
  ANALYZE bn_functions;
  ANALYZE bn_function_views;
  ANALYZE bn_views;
  ANALYZE bn_address_references;
  ANALYZE bn_expression_substitutions;
  ANALYZE bn_instructions;
  ANALYZE bn_codenode_instructions;

  END;
$$;


ALTER FUNCTION public.import(rawmoduleid integer, moduleid integer, userid integer) OWNER TO postgres;

--
-- Name: FUNCTION import(rawmoduleid integer, moduleid integer, userid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION import(rawmoduleid integer, moduleid integer, userid integer) IS 'This function performs all necessary conversions to transform a raw module from the exporter tables into a BinNavi type module.';


--
-- Name: load_code_nodes(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_code_nodes(viewid integer) RETURNS TABLE(module_id integer, instruction_address bigint, operand_position integer, view_id integer, node_id integer, parent_function bigint, local_code_node_comment integer, global_code_node_comment integer, local_instruction_comment integer, global_instruction_comment integer, instruction_data bytea, x double precision, y double precision, width double precision, height double precision, color integer, bordercolor integer, selected boolean, visible boolean, mnemonic text, architecture architecture_type, expression_tree_id integer, expression_tree_type integer, symbol text, immediate bigint, expression_tree_parent_id integer, replacement text, target bigint, expression_types_type integer, expression_types_offset integer, expression_types_position integer, expression_types_path integer[], address_references_type address_reference_type, function_address bigint, type_instance_id integer)
    LANGUAGE sql
    AS $_$

SELECT bi.module_id AS module_id,
       bi.address AS instruction_address,
       bo.position AS operand_position,
       bv.id AS view_id,
       bn.id AS node_id,
       parent_function,
       bcn.comment_id AS local_code_node_comment,
       bgnc.comment_id AS global_code_node_comment,
       bci.comment_id AS local_instruction_comment,
       bi.comment_id AS global_instruction_comment,
       bi.data AS instruction_data,
       x,
       y,
       width,
       height,
       color,
       bordercolor,
       selected,
       visible,
       mnemonic,
       architecture,
       bet.id AS expression_tree_id,
       bet.type AS expression_tree_type,
       symbol,
       immediate,
       bet.parent_id AS expression_tree_parent_id,
       replacement,
       target,
       bety.base_type_id AS expression_types_type,
       bety.offset AS expression_types_offset,
       bety.position AS expression_types_position,
       bety.path AS expression_types_path,
       bar.type AS address_references_type,
       bf.address AS function_address,
       beti.type_instance_id AS type_instance_id

FROM bn_views as bv
JOIN bn_nodes as bn
  ON bn.view_id = bv.id
 AND bn.type = 'code'::node_type

LEFT JOIN bn_code_nodes AS bcn
       ON bcn.node_id = bn.id

LEFT JOIN bn_codenode_instructions AS bci
       ON bci.module_id = bcn.module_id
      AND bci.node_id = bcn.node_id

LEFT JOIN bn_global_node_comments AS bgnc
       ON bgnc.module_id = bci.module_id
      AND bgnc.address = bci.address

LEFT JOIN bn_instructions AS bi
       ON bi.module_id = bci.module_id
      AND bi.address = bci.address

LEFT JOIN bn_operands AS bo
       ON bo.module_id = bi.module_id
      AND bo.address = bi.address

LEFT JOIN bn_expression_tree_mapping AS betm
       ON betm.module_id = bo.module_id
      AND betm.tree_id = bo.expression_tree_id

LEFT JOIN bn_expression_tree AS bet
       ON bet.module_id = betm.module_id
      and bet.id = betm.tree_node_id

LEFT JOIN bn_address_references AS bar
       ON bar.module_id = betm.module_id
      AND bar.address = bi.address
      AND bar.position = bo.position
      AND bar.expression_id = bet.id

LEFT JOIN bn_expression_substitutions AS bes
       ON bes.module_id = bet.module_id
      AND bes.address = bi.address
      AND bes.position = bo.position
      AND bes.expression_id = bet.id

LEFT JOIN bn_expression_types AS bety
       ON bety.module_id = bet.module_id
      AND bety.address = bi.address
      AND bety.expression_id = bet.id

LEFT JOIN bn_functions AS bf
       ON bf.module_id = bet.module_id
      AND bf.address = bet.immediate

LEFT JOIN bn_expression_type_instances AS beti
       ON beti.module_id = bet.module_id
      AND beti.address = bi.address
      AND beti.position = bo.position
      AND beti.expression_id = bet.id

WHERE bv.id = $1

ORDER BY bn.id, bci.position, bo.position, bet.position, bet.id;

$_$;


ALTER FUNCTION public.load_code_nodes(viewid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_code_nodes(viewid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_code_nodes(viewid integer) IS 'Loads the code nodes for a view.';


--
-- Name: load_expression_type_instance(integer, integer, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_expression_type_instance(moduleid integer, typeinstanceid integer, address bigint, "position" integer, expressionid integer) RETURNS TABLE(view_id integer, module_id integer, address bigint, "position" integer, expression_id integer, type_instance_id integer)
    LANGUAGE sql
    AS $_$

SELECT bv.id AS view_id, beti.module_id, beti.address, beti.position, beti.expression_id, beti.type_instance_id
  FROM bn_views AS bv
  JOIN bn_nodes AS bn
	ON bn.view_id = bv.id
   AND bn.type = 'code'::node_type
  LEFT JOIN bn_code_nodes AS bcn
         ON bcn.node_id = bn.id
  LEFT JOIN bn_codenode_instructions AS bcni
         ON bcni.module_id = bcn.module_id
        AND bcni.node_id = bcn.node_id
  LEFT JOIN bn_instructions AS bi
         ON bi.module_id = bcni.module_id
        AND bi.address = bcni.address
  JOIN bn_expression_type_instances AS beti
    ON beti.module_id = bi.module_id
   AND beti.address = bi.address
 WHERE bcn.module_id = $1
   AND beti.type_instance_id = $2
   AND beti.address = $3
   AND beti.position = $4
   AND beti.expression_id = $5

$_$;


ALTER FUNCTION public.load_expression_type_instance(moduleid integer, typeinstanceid integer, address bigint, "position" integer, expressionid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_expression_type_instance(moduleid integer, typeinstanceid integer, address bigint, "position" integer, expressionid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_expression_type_instance(moduleid integer, typeinstanceid integer, address bigint, "position" integer, expressionid integer) IS 'Loads a single cross reference from the database.';


--
-- Name: load_expression_type_instances(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_expression_type_instances(moduleid integer) RETURNS TABLE(view_id integer, module_id integer, address bigint, "position" integer, expression_id integer, type_instance_id integer)
    LANGUAGE sql
    AS $_$

SELECT bv.id AS view_id, beti.module_id, beti.address, beti.position, beti.expression_id, beti.type_instance_id
  FROM bn_views AS bv
  JOIN bn_nodes AS bn
	ON bn.view_id = bv.id
   AND bn.type = 'code'::node_type
  LEFT JOIN bn_code_nodes AS bcn
         ON bcn.node_id = bn.id
  LEFT JOIN bn_codenode_instructions AS bcni
         ON bcni.module_id = bcn.module_id
        AND bcni.node_id = bcn.node_id
  LEFT JOIN bn_instructions AS bi
         ON bi.module_id = bcni.module_id
        AND bi.address = bcni.address
  JOIN bn_expression_type_instances AS beti
    ON beti.module_id = bi.module_id
   AND beti.address = bi.address
 WHERE bcn.module_id = $1

$_$;


ALTER FUNCTION public.load_expression_type_instances(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_expression_type_instances(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_expression_type_instances(moduleid integer) IS 'The function retrieves all expression type instances for a given module.';


--
-- Name: load_function_information(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_function_information(moduleid integer) RETURNS TABLE(view_id integer, address bigint, name text, original_name text, description text, bbcount bigint, edgecount bigint, incount bigint, outcount bigint, global_comment integer, type function_type, parent_module_name text, parent_module_id integer, parent_module_function integer, stack_frame integer)
    LANGUAGE sql
    AS $_$

WITH function_block_count AS (
  SELECT function, count(nt.id) AS bbcount FROM bn_views AS vt
    LEFT JOIN bn_function_views AS fvt ON vt.id = fvt.view_id
    LEFT JOIN bn_nodes AS nt ON nt.view_id = vt.id
    WHERE fvt.module_id = $1
    GROUP BY fvt.function
), function_edge_count AS (
  SELECT function, count(et.id) AS edgecount FROM bn_views AS vt
    LEFT JOIN bn_function_views AS fvt ON vt.id = fvt.view_id
    LEFT JOIN bn_nodes AS nt ON nt.view_id = vt.id
    LEFT JOIN bn_edges AS et ON source_node_id = nt.id
    WHERE fvt.module_id = $1
    GROUP BY function
), function_in_count AS (
  SELECT function, COUNT(source_node_id) AS incount FROM bn_views AS vt
    JOIN bn_nodes AS nt ON nt.view_id = vt.id
    JOIN bn_function_nodes AS fnt ON nt.id = fnt.node_id
    LEFT JOIN bn_edges ON target_node_id = fnt.node_id
    WHERE vt.type = 'native' AND module_id = $1
    GROUP BY function
), function_out_count AS (
  SELECT function, COUNT(target_node_id) AS outcount FROM bn_views AS vt
    JOIN bn_nodes AS nt ON nt.view_id = vt.id
    JOIN bn_function_nodes AS fnt ON nt.id = fnt.node_id
    LEFT JOIN bn_edges ON source_node_id = fnt.node_id
    WHERE vt.type = 'native' and module_id = $1
    GROUP BY function
)
SELECT view_id, ft.address, name, original_name, description,
bc.bbcount, ec.edgecount, ic.incount, oc.outcount,
 comment_id AS global_comment, type, parent_module_name,
 parent_module_id, parent_module_function, stack_frame FROM bn_functions AS ft
 JOIN bn_function_views AS fviews ON fviews.module_id = ft.module_id
  AND function = ft.address
 JOIN function_block_count AS bc ON bc.function = ft.address
 JOIN function_edge_count AS ec ON ec.function = ft.address
 JOIN function_in_count AS ic ON ic.function = ft.address
 JOIN function_out_count AS oc ON oc.function = ft.address
 WHERE ft.module_id = $1
 ORDER BY ft.address

$_$;


ALTER FUNCTION public.load_function_information(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_function_information(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_function_information(moduleid integer) IS 'This function provides the information about all view / function information stored in the database under a specific module id.';


--
-- Name: load_function_information(integer, bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_function_information(moduleid integer, address bigint) RETURNS TABLE(view_id integer, address bigint, name text, original_name text, description text, bbcount bigint, edgecount bigint, incount bigint, outcount bigint, global_comment integer, type function_type, parent_module_name text, parent_module_id integer, parent_module_function integer)
    LANGUAGE sql
    AS $_$

WITH function_block_count AS (
  SELECT function, count(nt.id) AS bbcount FROM bn_views AS vt
    LEFT JOIN bn_function_views AS fvt ON vt.id = fvt.view_id
    LEFT JOIN bn_nodes AS nt ON nt.view_id = vt.id
    WHERE fvt.module_id = $1 AND function = $2
    GROUP BY fvt.function
), function_edge_count AS (
  SELECT function, count(et.id) AS edgecount FROM bn_views AS vt
    LEFT JOIN bn_function_views AS fvt ON vt.id = fvt.view_id
    LEFT JOIN bn_nodes AS nt ON nt.view_id = vt.id
    LEFT JOIN bn_edges AS et ON source_node_id = nt.id
    WHERE fvt.module_id = $1 AND function = $2
    GROUP BY function
), function_in_count AS (
  SELECT function, COUNT(source_node_id) AS incount FROM bn_views AS vt
    JOIN bn_nodes AS nt ON nt.view_id = vt.id
    JOIN bn_function_nodes AS fnt ON nt.id = fnt.node_id
    LEFT JOIN bn_edges ON target_node_id = fnt.node_id
    WHERE vt.type = 'native' AND module_id = $1 AND function = $2
    GROUP BY function
), function_out_count AS (
  SELECT function, COUNT(target_node_id) AS outcount FROM bn_views AS vt
    JOIN bn_nodes AS nt ON nt.view_id = vt.id
    JOIN bn_function_nodes AS fnt ON nt.id = fnt.node_id
    LEFT JOIN bn_edges ON source_node_id = fnt.node_id
    WHERE vt.type = 'native' AND module_id = $1 AND function = $2
    GROUP BY function
)
SELECT view_id, ft.address, name, original_name, description,
bc.bbcount, ec.edgecount, ic.incount, oc.outcount,
 comment_id AS global_comment, type, parent_module_name,
 parent_module_id, parent_module_function, stack_frame FROM bn_functions AS ft
 JOIN bn_function_views AS fviews ON fviews.module_id = ft.module_id
  AND function = ft.address
 JOIN function_block_count AS bc ON bc.function = ft.address
 JOIN function_edge_count AS ec ON ec.function = ft.address
 JOIN function_in_count AS ic ON ic.function = ft.address
 JOIN function_out_count AS oc ON oc.function = ft.address
 WHERE ft.module_id = $1 AND ft.address = $2

$_$;


ALTER FUNCTION public.load_function_information(moduleid integer, address bigint) OWNER TO postgres;

--
-- Name: FUNCTION load_function_information(moduleid integer, address bigint); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_function_information(moduleid integer, address bigint) IS 'This function provides the information about a single view / function information stored in the database.';


--
-- Name: load_global_variables(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_global_variables(moduleid integer) RETURNS TABLE(target bigint, replacement text, view_id integer)
    LANGUAGE sql
    AS $_$

WITH first AS (
  SELECT target, replacement, ref.module_id, ref.address FROM bn_address_references AS ref
    INNER JOIN bn_expression_substitutions AS sub ON sub.module_id = $1
      AND ref.module_id = $1
      AND sub.address = ref.address
      AND sub.position = ref.position
      AND sub.expression_id = ref.expression_id
      AND ref.type IN ( 'data', 'data_string' )
), second AS (
  SELECT target, replacement, node_id FROM first
    INNER JOIN bn_codenode_instructions AS ci ON ci.module_id = $1 AND ci.address = first.address
)
SELECT target, replacement, nodes.view_id FROM second
  INNER JOIN bn_nodes AS nodes ON nodes.id = second.node_id
  JOIN bn_module_views AS bmv ON bmv.view_id = nodes.view_id
    AND bmv.module_id = $1

$_$;


ALTER FUNCTION public.load_global_variables(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_global_variables(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_global_variables(moduleid integer) IS 'This function loads all global variables for a given module.';


--
-- Name: load_module_call_graph(integer, view_type); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_module_call_graph(moduleid integer, viewtype view_type) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type)
    LANGUAGE sql
    AS $_$
    SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
  COUNT(DISTINCT(nt.id)) AS bbcount,
  COUNT(et.id) AS edgecount,
  COUNT(DISTINCT(nt.type)) AS type_count,
  nt.type AS node_type
FROM bn_views AS vt
JOIN bn_module_views AS mvt ON vt.id = mvt.view_id
LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
  WHERE vt.type = $2 AND mvt.module_id = $1
GROUP BY vt.id, nt.type
HAVING COUNT(DISTINCT(nt.type)) = 1
  AND nt.type = 'function'
ORDER BY vt.id;
$_$;


ALTER FUNCTION public.load_module_call_graph(moduleid integer, viewtype view_type) OWNER TO postgres;

--
-- Name: FUNCTION load_module_call_graph(moduleid integer, viewtype view_type); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_module_call_graph(moduleid integer, viewtype view_type) IS 'This function loads module call graph information. But not the call graph itself.';


--
-- Name: load_module_call_graphs(integer, view_type); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_module_call_graphs(moduleid integer, viewtype view_type) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type)
    LANGUAGE sql
    AS $_$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count,
    MAX(nt.type) AS node_type
FROM bn_views AS vt
JOIN bn_module_views AS mvt ON vt.id = mvt.view_id
LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
  WHERE vt.type = $2
    AND mvt.module_id = $1
  GROUP BY vt.id
    HAVING COUNT(DISTINCT(nt.type)) = 1
    AND MAX(nt.type) = 'function'

$_$;


ALTER FUNCTION public.load_module_call_graphs(moduleid integer, viewtype view_type) OWNER TO postgres;

--
-- Name: FUNCTION load_module_call_graphs(moduleid integer, viewtype view_type); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_module_call_graphs(moduleid integer, viewtype view_type) IS 'This function loads call graph type graphs for a module.';


--
-- Name: load_module_flow_graphs(integer, view_type); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_module_flow_graphs(moduleid integer, viewtype view_type) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type)
    LANGUAGE sql
    AS $_$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count,
    MAX(nt.type) AS node_type
	FROM bn_views AS vt
	JOIN bn_module_views AS mvt ON vt.id = mvt.view_id
	LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
	LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
      WHERE vt.type = $2
      AND mvt.module_id = $1
    GROUP BY vt.id
      HAVING COUNT(DISTINCT(nt.type)) <= 1
      AND MAX(nt.type) = 'code' OR MAX(nt.type) is null

$_$;


ALTER FUNCTION public.load_module_flow_graphs(moduleid integer, viewtype view_type) OWNER TO postgres;

--
-- Name: FUNCTION load_module_flow_graphs(moduleid integer, viewtype view_type); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_module_flow_graphs(moduleid integer, viewtype view_type) IS 'This function loads flow graph type graphs for a module.';


--
-- Name: load_module_flowgraph_information(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_module_flowgraph_information(moduleid integer, viewid integer) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type)
    LANGUAGE sql
    AS $_$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count,
    nt.type AS node_type
    FROM bn_views AS vt
    JOIN bn_module_views AS mvt ON vt.id = mvt.view_id
    LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
    LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
      WHERE vt.id = $2
        AND mvt.module_id = $1
        AND (nt.type IN ('code', 'function') OR nt.type IS NULL)
      GROUP BY vt.id, nt.type

$_$;


ALTER FUNCTION public.load_module_flowgraph_information(moduleid integer, viewid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_module_flowgraph_information(moduleid integer, viewid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_module_flowgraph_information(moduleid integer, viewid integer) IS 'This function loads aggregated information about a specific view / function from the database.';


--
-- Name: load_module_mixed_graph(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_module_mixed_graph(moduleid integer) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint)
    LANGUAGE sql
    AS $_$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count
    FROM bn_views AS vt
    JOIN bn_module_views AS mvt ON vt.id = mvt.view_id
    LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
    LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
      WHERE vt.type = 'non-native' AND mvt.module_id = $1
      GROUP BY vt.id
      HAVING COUNT(DISTINCT(nt.type)) = 2

$_$;


ALTER FUNCTION public.load_module_mixed_graph(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_module_mixed_graph(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_module_mixed_graph(moduleid integer) IS 'This function loads all mixed graph information for the specified module from the database.';


--
-- Name: load_module_node_tags(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_module_node_tags(moduleid integer) RETURNS TABLE(view_id integer, tag_id integer)
    LANGUAGE sql
    AS $_$

  SELECT nt.view_id, tnt.tag_id FROM bn_tagged_nodes AS tnt
        JOIN bn_nodes AS nt ON node_id = nt.id
        JOIN bn_module_views AS mvt ON mvt.view_id = nt.view_id
      WHERE mvt.module_id = $1
      GROUP BY nt.view_id, tnt.tag_id
      ORDER BY nt.view_id;

$_$;


ALTER FUNCTION public.load_module_node_tags(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_module_node_tags(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_module_node_tags(moduleid integer) IS 'Loads the node tags for the module given as argument.';


--
-- Name: load_project_call_graph(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_project_call_graph(projectid integer) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type)
    LANGUAGE sql
    AS $_$

SELECT vt.id AS view_id, name, description,  vt.type AS type, creation_date, modification_date, stared,
count(distinct(nt.id)) AS bbcount, count(et.id) AS edgecount, count(distinct(nt.type)) AS type_count, nt.type AS node_type
FROM bn_views AS vt
JOIN bn_project_views AS pvt ON vt.id = pvt.view_id
LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
  WHERE pvt.project_id = $1
GROUP BY vt.id, nt.type
HAVING COUNT(DISTINCT(nt.type)) = 1
  AND nt.type = 'function'
ORDER BY vt.id;

$_$;


ALTER FUNCTION public.load_project_call_graph(projectid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_project_call_graph(projectid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_project_call_graph(projectid integer) IS 'This function loads project call graph information. But not the call graph itself.';


--
-- Name: load_project_call_graphs(integer, view_type); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_project_call_graphs(projectid integer, viewtype view_type) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type)
    LANGUAGE sql
    AS $_$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count,
    MAX(nt.type) AS node_type
FROM bn_views AS vt
JOIN bn_project_views AS pvt ON vt.id = pvt.view_id
LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
  WHERE vt.type = $2
    AND pvt.project_id = $1
  GROUP BY vt.id
    HAVING COUNT(DISTINCT(nt.type)) = 1
    AND MAX(nt.type) = 'function'

$_$;


ALTER FUNCTION public.load_project_call_graphs(projectid integer, viewtype view_type) OWNER TO postgres;

--
-- Name: FUNCTION load_project_call_graphs(projectid integer, viewtype view_type); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_project_call_graphs(projectid integer, viewtype view_type) IS 'This function loads call graph type graphs for a project.';


--
-- Name: load_project_flow_graphs(integer, view_type); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_project_flow_graphs(projectid integer, viewtype view_type) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type)
    LANGUAGE sql
    AS $_$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count,
    MAX(nt.type) AS node_type
	FROM bn_views AS vt
	JOIN bn_project_views AS pvt ON vt.id = pvt.view_id
	LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
	LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
      WHERE vt.type = $2
      AND pvt.project_id = $1
    GROUP BY vt.id
      HAVING COUNT(DISTINCT(nt.type)) <= 1
      AND MAX(nt.type) = 'code' OR MAX(nt.type) is null

$_$;


ALTER FUNCTION public.load_project_flow_graphs(projectid integer, viewtype view_type) OWNER TO postgres;

--
-- Name: FUNCTION load_project_flow_graphs(projectid integer, viewtype view_type); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_project_flow_graphs(projectid integer, viewtype view_type) IS 'This function loads flow graph type graphs for a project.';


--
-- Name: load_project_flowgraph(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_project_flowgraph(projectid integer, viewid integer) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type)
    LANGUAGE sql
    AS $_$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count,
    nt.type AS node_type
    FROM bn_views AS vt
    JOIN bn_project_views AS pvt ON vt.id = pvt.view_id
    LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
    LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
      WHERE vt.type = 'non-native'
        AND vt.id = $2
        AND pvt.project_id = $1
        AND nt.type in ('code', 'function')
      GROUP BY vt.id, nt.type
      HAVING COUNT(DISTINCT(nt.type)) = 1 AND nt.type = 'code'
      ORDER BY vt.id

$_$;


ALTER FUNCTION public.load_project_flowgraph(projectid integer, viewid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_project_flowgraph(projectid integer, viewid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_project_flowgraph(projectid integer, viewid integer) IS 'This function loads project view specific configuration information from the database.';


--
-- Name: load_project_mixed_graph(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_project_mixed_graph(projectid integer) RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint)
    LANGUAGE sql
    AS $_$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count
    FROM bn_views AS vt
    JOIN bn_project_views AS mvt ON vt.id = pvt.view_id
    LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
    LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
      WHERE vt.type = 'non-native' AND pvt.project_id = $1
      GROUP BY vt.id
      HAVING COUNT(DISTINCT(nt.type)) in (0,2)

$_$;


ALTER FUNCTION public.load_project_mixed_graph(projectid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_project_mixed_graph(projectid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_project_mixed_graph(projectid integer) IS 'This function loads all mixed graph information for the specified project from the database.';


--
-- Name: load_project_node_tags(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_project_node_tags(projectid integer) RETURNS TABLE(view_id integer, tag_id integer)
    LANGUAGE sql
    AS $_$

  SELECT nt.view_id, tnt.tag_id FROM bn_tagged_nodes AS tnt
        JOIN bn_nodes AS nt ON node_id = nt.id
        JOIN bn_project_views AS pvt ON pvt.view_id = nt.view_id
      WHERE pvt.project_id = $1
      GROUP BY nt.view_id, tnt.tag_id
      ORDER BY nt.view_id;

$_$;


ALTER FUNCTION public.load_project_node_tags(projectid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_project_node_tags(projectid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_project_node_tags(projectid integer) IS 'Loads the node tags for the project given as argument.';


--
-- Name: load_type(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_type(moduleid integer, typeid integer) RETURNS TABLE(id integer, name text, size integer, pointer integer, signed boolean, category type_category)
    LANGUAGE sql
    AS $_$

SELECT id, name, size, pointer, signed, category
  FROM bn_base_types
 WHERE module_id = $1
   AND id = $2;

$_$;


ALTER FUNCTION public.load_type(moduleid integer, typeid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_type(moduleid integer, typeid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_type(moduleid integer, typeid integer) IS 'Loads a single base type.';


--
-- Name: load_type_instance(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_type_instance(moduleid integer, typeinstanceid integer) RETURNS TABLE(module_id integer, id integer, name text, comment_id integer, type_id integer, section_id integer, section_offset bigint)
    LANGUAGE sql
    AS $_$
    SELECT * FROM bn_type_instances
     WHERE module_id = $1
	   AND id = $2;
$_$;


ALTER FUNCTION public.load_type_instance(moduleid integer, typeinstanceid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_type_instance(moduleid integer, typeinstanceid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_type_instance(moduleid integer, typeinstanceid integer) IS 'This function loads a single type instaces.';


--
-- Name: load_type_instances(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_type_instances(moduleid integer) RETURNS TABLE(module_id integer, id integer, name text, comment_id integer, type_id integer, section_id integer, section_offset bigint)
    LANGUAGE sql
    AS $_$
    SELECT * FROM bn_type_instances
     WHERE module_id = $1;

$_$;


ALTER FUNCTION public.load_type_instances(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_type_instances(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_type_instances(moduleid integer) IS 'This function retrives all type instaces for a given module id.';


--
-- Name: load_type_member(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_type_member(moduleid integer, typeid integer) RETURNS TABLE(id integer, name text, base_type integer, parent_id integer, "offset" integer, argument integer, number_of_elements integer)
    LANGUAGE sql
    AS $_$

SELECT id, name, base_type, parent_id, "offset", argument, number_of_elements
  FROM bn_types
 WHERE module_id = $1
   AND id = $2;

$_$;


ALTER FUNCTION public.load_type_member(moduleid integer, typeid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_type_member(moduleid integer, typeid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_type_member(moduleid integer, typeid integer) IS 'Loads a single type member from the database.';


--
-- Name: load_type_members(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_type_members(moduleid integer) RETURNS TABLE(id integer, name text, base_type integer, parent_id integer, "offset" integer, argument integer, number_of_elements integer)
    LANGUAGE sql
    AS $_$

SELECT id, name, base_type, parent_id, "offset", argument, number_of_elements
  FROM bn_types
 WHERE module_id = $1;

$_$;


ALTER FUNCTION public.load_type_members(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_type_members(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_type_members(moduleid integer) IS 'Loads all type members for the given module.';


--
-- Name: load_type_substitution(integer, bigint, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_type_substitution(moduleid integer, address bigint, "position" integer, expression_id integer) RETURNS TABLE(address bigint, "position" integer, expression_id integer, base_type_id integer, path integer[], "offset" integer)
    LANGUAGE sql
    AS $_$

SELECT address, "position", expression_id, base_type_id, path, "offset"
  FROM bn_expression_types
 WHERE module_id = $1
   AND address = $2
   AND "position" = $3
   AND expression_id = $4;

$_$;


ALTER FUNCTION public.load_type_substitution(moduleid integer, address bigint, "position" integer, expression_id integer) OWNER TO postgres;

--
-- Name: FUNCTION load_type_substitution(moduleid integer, address bigint, "position" integer, expression_id integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_type_substitution(moduleid integer, address bigint, "position" integer, expression_id integer) IS 'Loads a aingle type susbtitutions.';


--
-- Name: load_type_substitutions(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_type_substitutions(moduleid integer) RETURNS TABLE(address bigint, "position" integer, expression_id integer, base_type_id integer, path integer[], "offset" integer)
    LANGUAGE sql
    AS $_$

SELECT address, "position", expression_id, base_type_id, path, "offset"
  FROM bn_expression_types
 WHERE module_id = $1;

$_$;


ALTER FUNCTION public.load_type_substitutions(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_type_substitutions(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_type_substitutions(moduleid integer) IS 'Loads all type susbtitutions for a single module from the database.';


--
-- Name: load_types(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_types(moduleid integer) RETURNS TABLE(id integer, name text, size integer, pointer integer, signed boolean, category type_category)
    LANGUAGE sql
    AS $_$

SELECT id, name, size, pointer, signed, category
  FROM bn_base_types
 WHERE module_id = $1;

$_$;


ALTER FUNCTION public.load_types(moduleid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_types(moduleid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_types(moduleid integer) IS 'Loads all base types for a given module id.';


--
-- Name: load_view_edges(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION load_view_edges(viewid integer) RETURNS TABLE(id integer, source_node_id integer, target_node_id integer, comment_id integer, x1 double precision, y1 double precision, x2 double precision, y2 double precision, type edge_type, color integer, visible boolean, selected boolean, x double precision, y double precision)
    LANGUAGE sql
    AS $_$
  SELECT edges.id, source_node_id, target_node_id,
  comment_id, x1, y1, x2, y2,
  edges.type, edges.color, edges.visible, edges.selected, ep.x, ep.y
  FROM bn_edges AS edges
    JOIN bn_nodes AS bt ON edges.target_node_id = bt.id
    JOIN bn_nodes AS bs ON edges.source_node_id = bs.id
    LEFT JOIN bn_edge_paths AS ep ON ep.edge_id = edges.id
  WHERE bt.view_id = $1
    AND bs.view_id = $1;
$_$;


ALTER FUNCTION public.load_view_edges(viewid integer) OWNER TO postgres;

--
-- Name: FUNCTION load_view_edges(viewid integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION load_view_edges(viewid integer) IS 'This function loads the edges of a view.';


--
-- Name: locate_type(integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION locate_type(moduleid integer, parentid integer, currentoffset integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
SELECT bt.id FROM bn_types AS bt
  JOIN bn_base_types AS bbt
    ON bt.base_type = bbt.id
   AND bt.module_id = bbt.module_id
 WHERE bt.module_id = moduleid
   AND bt.parent_id = parentid
   AND bt.offset <= currentoffset
   AND bt.offset + bbt.size >= currentoffset
$$;


ALTER FUNCTION public.locate_type(moduleid integer, parentid integer, currentoffset integer) OWNER TO postgres;

--
-- Name: FUNCTION locate_type(moduleid integer, parentid integer, currentoffset integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION locate_type(moduleid integer, parentid integer, currentoffset integer) IS 'Locates the bn_type id of a type at a given offset.';


--
-- Name: move_type(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION move_type(moduleid integer, old_parent_id integer, new_parent_id integer, type_id integer, newoffset integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  oldoffset integer;
BEGIN
  --
  -- Determine if the parent ids are the same
  --
  IF old_parent_id = new_parent_id THEN
        --
	-- same struct move
	--
    SELECT offset FROM bn_types
	  WHERE module_id = moduleid
	    AND id = type_id
	   INTO oldoffset;

	IF newoffset > oldoffset THEN
	  --
      -- we have moved the member up.
      --

	ELSE
	  --
	  -- we have moved the member down.
	  --

	END IF;
  ELSE
    --
	-- different struct move
	--

  END IF;
END;
$$;


ALTER FUNCTION public.move_type(moduleid integer, old_parent_id integer, new_parent_id integer, type_id integer, newoffset integer) OWNER TO postgres;

--
-- Name: set_section_name(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION set_section_name(moduleid integer, sectionid integer, newname text) RETURNS void
    LANGUAGE sql
    AS $_$

    UPDATE bn_sections
       SET name = $3
     WHERE module_id = $1
       AND id = $2

$_$;


ALTER FUNCTION public.set_section_name(moduleid integer, sectionid integer, newname text) OWNER TO postgres;

--
-- Name: FUNCTION set_section_name(moduleid integer, sectionid integer, newname text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION set_section_name(moduleid integer, sectionid integer, newname text) IS 'This function creates a new entry in the section table of BinNavi and returns the id of the entry.';


--
-- Name: set_type_instance_name(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION set_type_instance_name(moduleid integer, typeinstanceid integer, newname text) RETURNS void
    LANGUAGE sql
    AS $_$

    UPDATE bn_type_instances
       SET name = $3
     WHERE module_id = $1
       AND id = $2;

$_$;


ALTER FUNCTION public.set_type_instance_name(moduleid integer, typeinstanceid integer, newname text) OWNER TO postgres;

--
-- Name: FUNCTION set_type_instance_name(moduleid integer, typeinstanceid integer, newname text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION set_type_instance_name(moduleid integer, typeinstanceid integer, newname text) IS 'This functions sets the name of a type instance.';


--
-- Name: update_member_offsets(integer, integer[], integer, integer[], integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION update_member_offsets(module_id integer, updated_members integer[], delta integer, implicitly_updated_members integer[], implicit_delta integer) RETURNS void
    LANGUAGE sql
    AS $_$
WITH moved AS (
  UPDATE bn_types
  SET "offset" = "offset" + $3
  WHERE module_id = $1
    AND id = any($2)
)
UPDATE bn_types
SET "offset" = "offset" + $5
WHERE module_id = $1
  AND id = any($4);
$_$;


ALTER FUNCTION public.update_member_offsets(module_id integer, updated_members integer[], delta integer, implicitly_updated_members integer[], implicit_delta integer) OWNER TO postgres;

--
-- Name: FUNCTION update_member_offsets(module_id integer, updated_members integer[], delta integer, implicitly_updated_members integer[], implicit_delta integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION update_member_offsets(module_id integer, updated_members integer[], delta integer, implicitly_updated_members integer[], implicit_delta integer) IS 'This function adjusts the offsets of the updated_members by delta and the offsets of implicitly_updated_members by implicit_dela, respectively.';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: bn_address_references; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_address_references (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    type address_reference_type NOT NULL,
    target bigint NOT NULL
);


ALTER TABLE public.bn_address_references OWNER TO postgres;

--
-- Name: TABLE bn_address_references; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_address_references IS 'This table stores all address references.';


--
-- Name: COLUMN bn_address_references.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_references.module_id IS 'The module id the address reference is associated to.';


--
-- Name: COLUMN bn_address_references.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_references.address IS 'The address where the reference is associated to.';


--
-- Name: COLUMN bn_address_references."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_references."position" IS 'The position of the operand tree to which the address reference is associated to.';


--
-- Name: COLUMN bn_address_references.expression_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_references.expression_id IS 'The id of the expression in the operand tree the address reference is associated to.';


--
-- Name: COLUMN bn_address_references.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_references.type IS 'The type of the address reference see the address_reference_type type for details.';


--
-- Name: COLUMN bn_address_references.target; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_references.target IS 'The target where address reference points to.';


--
-- Name: bn_address_spaces; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_address_spaces (
    id integer NOT NULL,
    project_id integer NOT NULL,
    debugger_id integer,
    name text NOT NULL,
    description text NOT NULL,
    creation_date timestamp without time zone DEFAULT now() NOT NULL,
    modification_date timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.bn_address_spaces OWNER TO postgres;

--
-- Name: TABLE bn_address_spaces; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_address_spaces IS 'This table stores the information about address spaces associated to a project.';


--
-- Name: COLUMN bn_address_spaces.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_spaces.id IS 'The id of the address space which is backed by the sequence bn_address_spaces_id_seq';


--
-- Name: COLUMN bn_address_spaces.project_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_spaces.project_id IS 'The id of the project the address space is associated to.';


--
-- Name: COLUMN bn_address_spaces.debugger_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_spaces.debugger_id IS 'The id of the current debugger of the address space.';


--
-- Name: COLUMN bn_address_spaces.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_spaces.name IS 'The name of the address space.';


--
-- Name: COLUMN bn_address_spaces.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_spaces.description IS 'The description of the address space.';


--
-- Name: COLUMN bn_address_spaces.creation_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_spaces.creation_date IS 'The creation date of the address space.';


--
-- Name: COLUMN bn_address_spaces.modification_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_address_spaces.modification_date IS 'The modification date of the address space.';


--
-- Name: bn_address_spaces_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_address_spaces_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_address_spaces_id_seq OWNER TO postgres;

--
-- Name: bn_address_spaces_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_address_spaces_id_seq OWNED BY bn_address_spaces.id;


--
-- Name: SEQUENCE bn_address_spaces_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_address_spaces_id_seq IS 'This sequence is used by the table bn_address_spaces id field.';


--
-- Name: bn_base_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_base_types (
    module_id integer NOT NULL,
    id integer NOT NULL,
    name text NOT NULL,
    size integer NOT NULL,
    pointer integer,
    signed boolean,
    category type_category NOT NULL
);


ALTER TABLE public.bn_base_types OWNER TO postgres;

--
-- Name: TABLE bn_base_types; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_base_types IS 'This table stores all base types for the type system used in BinNavi.';


--
-- Name: COLUMN bn_base_types.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_base_types.module_id IS 'The module id the base type is associated to.';


--
-- Name: COLUMN bn_base_types.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_base_types.id IS 'The id of the base type.';


--
-- Name: COLUMN bn_base_types.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_base_types.name IS 'The name of the base type.';


--
-- Name: COLUMN bn_base_types.size; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_base_types.size IS 'The size of the base type in bits.';


--
-- Name: COLUMN bn_base_types.pointer; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_base_types.pointer IS 'A flag that indicates if the base type is a pointer or not.';


--
-- Name: COLUMN bn_base_types.signed; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_base_types.signed IS 'A flag that indicates if the base type id signed or not.';


--
-- Name: COLUMN bn_base_types.category; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_base_types.category IS 'An enum that describes the category of this base type.';


--
-- Name: bn_base_types_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_base_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_base_types_id_seq OWNER TO postgres;

--
-- Name: bn_base_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_base_types_id_seq OWNED BY bn_base_types.id;


--
-- Name: SEQUENCE bn_base_types_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_base_types_id_seq IS 'This sequence is used by the table bn_base_types id field.';


--
-- Name: bn_code_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_code_nodes (
    module_id integer NOT NULL,
    node_id integer NOT NULL,
    parent_function bigint,
    comment_id integer
);


ALTER TABLE public.bn_code_nodes OWNER TO postgres;

--
-- Name: TABLE bn_code_nodes; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_code_nodes IS 'This table stores all information about code nodes.';


--
-- Name: COLUMN bn_code_nodes.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_code_nodes.module_id IS 'The module id the code node belongs to.';


--
-- Name: COLUMN bn_code_nodes.node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_code_nodes.node_id IS 'The node id the code node is associated with.';


--
-- Name: COLUMN bn_code_nodes.parent_function; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_code_nodes.parent_function IS 'The parent function of the code node.';


--
-- Name: COLUMN bn_code_nodes.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_code_nodes.comment_id IS 'The id of the comment associacted with the code node.';


--
-- Name: bn_codenode_instructions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_codenode_instructions (
    module_id integer NOT NULL,
    node_id integer NOT NULL,
    "position" integer NOT NULL,
    address bigint NOT NULL,
    comment_id integer
);


ALTER TABLE public.bn_codenode_instructions OWNER TO postgres;

--
-- Name: TABLE bn_codenode_instructions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_codenode_instructions IS 'This table stores the association between instructions and code nodes.';


--
-- Name: COLUMN bn_codenode_instructions.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_codenode_instructions.module_id IS 'The module id the association between instruction and code node belongs to.';


--
-- Name: COLUMN bn_codenode_instructions.node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_codenode_instructions.node_id IS 'The node id to which the instruction is associated.';


--
-- Name: COLUMN bn_codenode_instructions."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_codenode_instructions."position" IS 'The position within the node the instruction has.';


--
-- Name: COLUMN bn_codenode_instructions.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_codenode_instructions.address IS 'The address of the code node.';


--
-- Name: COLUMN bn_codenode_instructions.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_codenode_instructions.comment_id IS 'The id of the comment associated with the instruction in the code node.';


--
-- Name: bn_comments; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_comments (
    id integer NOT NULL,
    parent_id integer,
    user_id integer NOT NULL,
    comment_text text NOT NULL
);


ALTER TABLE public.bn_comments OWNER TO postgres;

--
-- Name: TABLE bn_comments; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_comments IS 'This table stores all information about comments';


--
-- Name: COLUMN bn_comments.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments.id IS 'The id of the comment used in all other tables as reference to this table.';


--
-- Name: COLUMN bn_comments.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments.parent_id IS 'This column contains the id of the comment which is the parent of this comment.nThe idea here is that the latest generated comment will be refered to by the comment_id in the tables having a comment such that we will traverse the comments upwards to generate the complete comment stream ';


--
-- Name: COLUMN bn_comments.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments.user_id IS 'The owner of the comment which is the only person to be able to delete or edit this comment.nThis value does not provide any security it is just there to reduce concurrent modification problems.';


--
-- Name: COLUMN bn_comments.comment_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments.comment_text IS 'The actual comment.';


--
-- Name: bn_comments_audit; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_comments_audit (
    operation bpchar NOT NULL,
    time_stamp timestamp with time zone DEFAULT ('now'::text)::date NOT NULL,
    id integer NOT NULL,
    parent_id integer,
    user_id integer NOT NULL,
    comment_text text NOT NULL
);


ALTER TABLE public.bn_comments_audit OWNER TO postgres;

--
-- Name: TABLE bn_comments_audit; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_comments_audit IS 'This table contains all operations that have been performed on the table bn_comments.nIts purpose is that for all operations done by multiple clients on the table bn_comments there will be a log of thier activity which can help debug issues if something goes wrong.';


--
-- Name: COLUMN bn_comments_audit.operation; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments_audit.operation IS 'The operation that has been performed where: U is update, I is insert and D is delete.';


--
-- Name: COLUMN bn_comments_audit.time_stamp; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments_audit.time_stamp IS 'The time stamp of the operation such that it is possible to find out when a speciffic event has occured.';


--
-- Name: COLUMN bn_comments_audit.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments_audit.id IS 'see bn_comments.id for description.';


--
-- Name: COLUMN bn_comments_audit.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments_audit.parent_id IS 'see bn_comments.parent_id for description.';


--
-- Name: COLUMN bn_comments_audit.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments_audit.user_id IS 'see bn_comments.user_id for description.';


--
-- Name: COLUMN bn_comments_audit.comment_text; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_comments_audit.comment_text IS 'see bn_comments.comment_text for description.';


--
-- Name: bn_comments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_comments_id_seq OWNER TO postgres;

--
-- Name: bn_comments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_comments_id_seq OWNED BY bn_comments.id;


--
-- Name: SEQUENCE bn_comments_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_comments_id_seq IS 'This sequence is used by the table bn_comments id field.';


--
-- Name: bn_data_parts; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_data_parts (
    module_id integer NOT NULL,
    part_id integer NOT NULL,
    data bytea
);


ALTER TABLE public.bn_data_parts OWNER TO postgres;

--
-- Name: TABLE bn_data_parts; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_data_parts IS 'This table is used to store the original binary data of the module in the database.';


--
-- Name: COLUMN bn_data_parts.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_data_parts.module_id IS 'Module id of a data part.';


--
-- Name: COLUMN bn_data_parts.part_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_data_parts.part_id IS 'Id of a data part.';


--
-- Name: COLUMN bn_data_parts.data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_data_parts.data IS 'The actual data of a data part.';


--
-- Name: bn_debuggers; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_debuggers (
    id integer NOT NULL,
    name text NOT NULL,
    host text NOT NULL,
    port integer NOT NULL
);


ALTER TABLE public.bn_debuggers OWNER TO postgres;

--
-- Name: TABLE bn_debuggers; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_debuggers IS 'This table contains all information to connect to a debug client.';


--
-- Name: COLUMN bn_debuggers.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_debuggers.id IS 'Id of the debug client.';


--
-- Name: COLUMN bn_debuggers.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_debuggers.name IS 'Name of the debug client.';


--
-- Name: COLUMN bn_debuggers.host; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_debuggers.host IS 'Host name of the debug client.';


--
-- Name: COLUMN bn_debuggers.port; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_debuggers.port IS 'Port number of the debug client.';


--
-- Name: bn_debuggers_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_debuggers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_debuggers_id_seq OWNER TO postgres;

--
-- Name: bn_debuggers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_debuggers_id_seq OWNED BY bn_debuggers.id;


--
-- Name: SEQUENCE bn_debuggers_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_debuggers_id_seq IS 'This sequence is used by the table bn_debuggers id field.';


--
-- Name: bn_edge_paths; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_edge_paths (
    edge_id integer NOT NULL,
    "position" integer NOT NULL,
    x double precision NOT NULL,
    y double precision NOT NULL
);


ALTER TABLE public.bn_edge_paths OWNER TO postgres;

--
-- Name: TABLE bn_edge_paths; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_edge_paths IS 'This table stores the layout information of edges.';


--
-- Name: COLUMN bn_edge_paths.edge_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edge_paths.edge_id IS 'The id of the edge which the path information belongs to.';


--
-- Name: COLUMN bn_edge_paths."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edge_paths."position" IS 'The position of the edge path.';


--
-- Name: COLUMN bn_edge_paths.x; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edge_paths.x IS 'The x coordinate of the edge path';


--
-- Name: COLUMN bn_edge_paths.y; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edge_paths.y IS 'The y coordinate of the edge path';


--
-- Name: bn_edges; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_edges (
    id integer NOT NULL,
    source_node_id integer NOT NULL,
    target_node_id integer NOT NULL,
    x1 double precision NOT NULL,
    y1 double precision NOT NULL,
    x2 double precision NOT NULL,
    y2 double precision NOT NULL,
    type edge_type NOT NULL,
    color integer NOT NULL,
    visible boolean NOT NULL,
    selected boolean NOT NULL,
    comment_id integer
);


ALTER TABLE public.bn_edges OWNER TO postgres;

--
-- Name: TABLE bn_edges; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_edges IS 'This table stores information about edges.';


--
-- Name: COLUMN bn_edges.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.id IS 'The id of the edge Globally unique in a single database.';


--
-- Name: COLUMN bn_edges.source_node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.source_node_id IS 'The id of the node where the edge originates from.';


--
-- Name: COLUMN bn_edges.target_node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.target_node_id IS 'The id of the node where the edge destined to.';


--
-- Name: COLUMN bn_edges.x1; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.x1 IS 'The x1 coordinate of the edge.';


--
-- Name: COLUMN bn_edges.y1; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.y1 IS 'The y1 coordinate of the edge.';


--
-- Name: COLUMN bn_edges.x2; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.x2 IS 'The x2 coordinate of the edge.';


--
-- Name: COLUMN bn_edges.y2; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.y2 IS 'The y2 coordinate of the edge.';


--
-- Name: COLUMN bn_edges.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.type IS 'The type of the edge see edge_type for all possible cases.';


--
-- Name: COLUMN bn_edges.color; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.color IS 'The color of the edge.';


--
-- Name: COLUMN bn_edges.visible; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.visible IS 'Flags if the edge is currently visible or not.';


--
-- Name: COLUMN bn_edges.selected; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.selected IS 'Flags if the edge is currently selected or not.';


--
-- Name: COLUMN bn_edges.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_edges.comment_id IS 'The id of the last comment in the comment list associated to the edge.';


--
-- Name: bn_edges_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_edges_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_edges_id_seq OWNER TO postgres;

--
-- Name: bn_edges_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_edges_id_seq OWNED BY bn_edges.id;


--
-- Name: SEQUENCE bn_edges_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_edges_id_seq IS 'This sequence is used by the table bn_edges id field.';


--
-- Name: bn_expression_substitutions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_expression_substitutions (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    replacement text NOT NULL
);


ALTER TABLE public.bn_expression_substitutions OWNER TO postgres;

--
-- Name: TABLE bn_expression_substitutions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_expression_substitutions IS 'This table defines the expression substitutions.';


--
-- Name: COLUMN bn_expression_substitutions.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_substitutions.module_id IS 'The id of the module to which this expression substitution belongs.';


--
-- Name: COLUMN bn_expression_substitutions.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_substitutions.address IS 'The address of the expression substitution.';


--
-- Name: COLUMN bn_expression_substitutions."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_substitutions."position" IS 'The position of the expression substitution in regards to the operands.';


--
-- Name: COLUMN bn_expression_substitutions.expression_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_substitutions.expression_id IS 'The id of the expression to be substituted.';


--
-- Name: COLUMN bn_expression_substitutions.replacement; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_substitutions.replacement IS 'The text replacement for this expression substitution.';


--
-- Name: bn_expression_tree; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_expression_tree (
    module_id integer NOT NULL,
    id integer NOT NULL,
    type integer NOT NULL,
    symbol character varying(256),
    immediate bigint,
    "position" integer NOT NULL,
    parent_id integer
);


ALTER TABLE public.bn_expression_tree OWNER TO postgres;

--
-- Name: TABLE bn_expression_tree; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_expression_tree IS 'This table defines the in BinNavi used expression trees';


--
-- Name: COLUMN bn_expression_tree.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree.module_id IS 'The id of the module tio which the expression tree belongs.';


--
-- Name: COLUMN bn_expression_tree.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree.id IS 'The id of the expression tree.';


--
-- Name: COLUMN bn_expression_tree.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree.type IS 'The type of the expression tree.';


--
-- Name: COLUMN bn_expression_tree.symbol; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree.symbol IS 'If the type is a symbol the string is saved here.';


--
-- Name: COLUMN bn_expression_tree.immediate; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree.immediate IS 'If the type is an immediate the immediate is saved here.';


--
-- Name: COLUMN bn_expression_tree."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree."position" IS 'The position of the expression tree.';


--
-- Name: COLUMN bn_expression_tree.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree.parent_id IS 'If the tree has a parent tree id it is saved here.';


--
-- Name: bn_expression_tree_ids; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_expression_tree_ids (
    module_id integer NOT NULL,
    id integer NOT NULL
);


ALTER TABLE public.bn_expression_tree_ids OWNER TO postgres;

--
-- Name: TABLE bn_expression_tree_ids; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_expression_tree_ids IS 'This table links expression tree ids to module ids.';


--
-- Name: COLUMN bn_expression_tree_ids.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree_ids.module_id IS 'Module id.';


--
-- Name: COLUMN bn_expression_tree_ids.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree_ids.id IS 'Expression tree id.';


--
-- Name: bn_expression_tree_mapping; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_expression_tree_mapping (
    module_id integer NOT NULL,
    tree_id integer NOT NULL,
    tree_node_id integer NOT NULL
);


ALTER TABLE public.bn_expression_tree_mapping OWNER TO postgres;

--
-- Name: TABLE bn_expression_tree_mapping; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_expression_tree_mapping IS 'This table maps a tree id of an expression tree to a tree node id of an expression tree.';


--
-- Name: COLUMN bn_expression_tree_mapping.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree_mapping.module_id IS 'The module id of the mapping.';


--
-- Name: COLUMN bn_expression_tree_mapping.tree_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree_mapping.tree_id IS 'The tree id of the mapping.';


--
-- Name: COLUMN bn_expression_tree_mapping.tree_node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_tree_mapping.tree_node_id IS 'The tree node id of the mapping.';


--
-- Name: bn_expression_type_instances; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_expression_type_instances (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    type_instance_id integer NOT NULL
);


ALTER TABLE public.bn_expression_type_instances OWNER TO postgres;

--
-- Name: TABLE bn_expression_type_instances; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_expression_type_instances IS 'This table stores the information about data cross references. It provides the link between a type instance and an operand tree expression in the graph.';


--
-- Name: COLUMN bn_expression_type_instances.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_type_instances.module_id IS 'The module id of the module this data xref belongs to.';


--
-- Name: COLUMN bn_expression_type_instances.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_type_instances.address IS 'The address of the instruction to which the type instance substitution belongs.';


--
-- Name: COLUMN bn_expression_type_instances."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_type_instances."position" IS 'The position of the operand within the instruction the type instance substitution belongs to.';


--
-- Name: COLUMN bn_expression_type_instances.expression_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_type_instances.expression_id IS 'The expression tree id in the operand the type instance belongs to.';


--
-- Name: COLUMN bn_expression_type_instances.type_instance_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_type_instances.type_instance_id IS 'The type instance to which this substitution points.';


--
-- Name: bn_expression_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_expression_types (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    base_type_id integer NOT NULL,
    path integer[],
    "offset" integer
);


ALTER TABLE public.bn_expression_types OWNER TO postgres;

--
-- Name: TABLE bn_expression_types; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_expression_types IS 'This table stores the type from the type system for a specific operand.';


--
-- Name: COLUMN bn_expression_types.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_types.module_id IS 'The module id the type is associated with.';


--
-- Name: COLUMN bn_expression_types.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_types.address IS 'The address where the type association is located.';


--
-- Name: COLUMN bn_expression_types."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_types."position" IS 'The position or the operand tree in the instruction where the type is associated to.';


--
-- Name: COLUMN bn_expression_types.expression_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_types.expression_id IS 'The expression tree id the type is associated to.';


--
-- Name: COLUMN bn_expression_types.base_type_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_types.base_type_id IS 'The bn_base_types type which is associated here.';


--
-- Name: COLUMN bn_expression_types.path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_types.path IS 'The path of the type substitution. Each integer here is an element from bn_types.';


--
-- Name: COLUMN bn_expression_types."offset"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_expression_types."offset" IS 'The offset of the type substitution.';


--
-- Name: bn_function_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_function_nodes (
    module_id integer NOT NULL,
    node_id integer NOT NULL,
    function bigint NOT NULL,
    comment_id integer
);


ALTER TABLE public.bn_function_nodes OWNER TO postgres;

--
-- Name: TABLE bn_function_nodes; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_function_nodes IS 'This table holds the information about function nodes in a module.';


--
-- Name: COLUMN bn_function_nodes.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_function_nodes.module_id IS 'The module id of the function node.';


--
-- Name: COLUMN bn_function_nodes.node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_function_nodes.node_id IS 'The node id the function node is associated to.';


--
-- Name: COLUMN bn_function_nodes.function; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_function_nodes.function IS 'The function address the function node is associated to.';


--
-- Name: COLUMN bn_function_nodes.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_function_nodes.comment_id IS 'The id of the comment associated to the function node.';


--
-- Name: bn_function_views; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_function_views (
    module_id integer NOT NULL,
    view_id integer NOT NULL,
    function bigint NOT NULL
);


ALTER TABLE public.bn_function_views OWNER TO postgres;

--
-- Name: TABLE bn_function_views; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_function_views IS 'This table holds the information about function views.';


--
-- Name: COLUMN bn_function_views.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_function_views.module_id IS 'The module id the function view is associated to.';


--
-- Name: COLUMN bn_function_views.view_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_function_views.view_id IS 'The view id the function view is associated to.';


--
-- Name: COLUMN bn_function_views.function; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_function_views.function IS 'The address of the function the function view is associated to.';


--
-- Name: bn_functions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_functions (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    name text,
    original_name text NOT NULL,
    type function_type NOT NULL,
    description text,
    parent_module_name text,
    parent_module_id integer,
    parent_module_function integer,
    stack_frame integer,
    comment_id integer
);


ALTER TABLE public.bn_functions OWNER TO postgres;

--
-- Name: TABLE bn_functions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_functions IS 'This table holds the information about functions and thier relations.';


--
-- Name: COLUMN bn_functions.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.module_id IS 'The id of the module the function belongs to.';


--
-- Name: COLUMN bn_functions.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.address IS 'The address of the function.';


--
-- Name: COLUMN bn_functions.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.name IS 'The current name of the function.';


--
-- Name: COLUMN bn_functions.original_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.original_name IS 'The original name of the function.';


--
-- Name: COLUMN bn_functions.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.type IS 'The type of the function see the function_type type for more information.';


--
-- Name: COLUMN bn_functions.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.description IS 'The description of the function.';


--
-- Name: COLUMN bn_functions.parent_module_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.parent_module_name IS 'If the function is forwarded the module name of the function where this function is forwarded to.';


--
-- Name: COLUMN bn_functions.parent_module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.parent_module_id IS 'If the function is forwarded the module id of the function where this function is forwarded to.';


--
-- Name: COLUMN bn_functions.parent_module_function; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.parent_module_function IS 'If the function is forwarded the address of the function where this function is forwarded to.';


--
-- Name: COLUMN bn_functions.stack_frame; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.stack_frame IS 'The bn_base_types id of the stack frame that is associated with the function.';


--
-- Name: COLUMN bn_functions.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_functions.comment_id IS 'The id of the comment associated with the function.';


--
-- Name: bn_global_edge_comments; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_global_edge_comments (
    src_module_id integer NOT NULL,
    dst_module_id integer NOT NULL,
    src_address bigint NOT NULL,
    dst_address bigint NOT NULL,
    comment_id integer
);


ALTER TABLE public.bn_global_edge_comments OWNER TO postgres;

--
-- Name: TABLE bn_global_edge_comments; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_global_edge_comments IS 'This table holds all global edge comments.';


--
-- Name: COLUMN bn_global_edge_comments.src_module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_global_edge_comments.src_module_id IS 'The module id of the module where the edge originates from.';


--
-- Name: COLUMN bn_global_edge_comments.dst_module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_global_edge_comments.dst_module_id IS 'The module if of the module where the edge destined to.';


--
-- Name: COLUMN bn_global_edge_comments.src_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_global_edge_comments.src_address IS 'The address of the source node of the edge.';


--
-- Name: COLUMN bn_global_edge_comments.dst_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_global_edge_comments.dst_address IS 'The address of the destination node of the edge.';


--
-- Name: COLUMN bn_global_edge_comments.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_global_edge_comments.comment_id IS 'The id of the global comment';


--
-- Name: bn_global_node_comments; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_global_node_comments (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    comment_id integer
);


ALTER TABLE public.bn_global_node_comments OWNER TO postgres;

--
-- Name: TABLE bn_global_node_comments; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_global_node_comments IS 'This table holds all global node comments.';


--
-- Name: COLUMN bn_global_node_comments.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_global_node_comments.module_id IS 'The module id of the node.';


--
-- Name: COLUMN bn_global_node_comments.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_global_node_comments.address IS 'The address of the node.';


--
-- Name: COLUMN bn_global_node_comments.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_global_node_comments.comment_id IS 'The id of the global node comment.';


--
-- Name: bn_group_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_group_nodes (
    node_id integer NOT NULL,
    collapsed boolean NOT NULL,
    comment_id integer
);


ALTER TABLE public.bn_group_nodes OWNER TO postgres;

--
-- Name: TABLE bn_group_nodes; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_group_nodes IS 'This table holds the information about group nodes.';


--
-- Name: COLUMN bn_group_nodes.node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_group_nodes.node_id IS 'The node id of the group node.';


--
-- Name: COLUMN bn_group_nodes.collapsed; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_group_nodes.collapsed IS 'Flag that indicates if the node is collapsed.';


--
-- Name: COLUMN bn_group_nodes.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_group_nodes.comment_id IS 'The id of the comment associated to the group node.';


--
-- Name: bn_instructions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_instructions (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    mnemonic character varying(32) NOT NULL,
    data bytea NOT NULL,
    native boolean NOT NULL,
    architecture architecture_type NOT NULL,
    comment_id integer
);


ALTER TABLE public.bn_instructions OWNER TO postgres;

--
-- Name: TABLE bn_instructions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_instructions IS 'This table holds the information about instruction.';


--
-- Name: COLUMN bn_instructions.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_instructions.module_id IS 'The module of the instruction.';


--
-- Name: COLUMN bn_instructions.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_instructions.address IS 'The address of the instruction.';


--
-- Name: COLUMN bn_instructions.mnemonic; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_instructions.mnemonic IS 'The mnemonic of the instruction.';


--
-- Name: COLUMN bn_instructions.data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_instructions.data IS 'The raw bytes of the instruction from the binary.';


--
-- Name: COLUMN bn_instructions.native; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_instructions.native IS 'Flag that indicates if the instruction has been build within BinNavi or came from external sources.';


--
-- Name: COLUMN bn_instructions.architecture; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_instructions.architecture IS 'The architecture of the instruction for more information about known architecture types see srchitecture_type type.';


--
-- Name: COLUMN bn_instructions.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_instructions.comment_id IS 'The id of the comment associated to the instruction.';


--
-- Name: bn_module_settings; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_module_settings (
    module_id integer NOT NULL,
    name character varying(255) NOT NULL,
    value text NOT NULL
);


ALTER TABLE public.bn_module_settings OWNER TO postgres;

--
-- Name: TABLE bn_module_settings; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_module_settings IS 'This table stores various settings for modules.';


--
-- Name: COLUMN bn_module_settings.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_module_settings.module_id IS 'The module id of the setting.';


--
-- Name: COLUMN bn_module_settings.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_module_settings.name IS 'The name of the setting.';


--
-- Name: COLUMN bn_module_settings.value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_module_settings.value IS 'The value of the setting.';


--
-- Name: bn_module_traces; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_module_traces (
    module_id integer NOT NULL,
    trace_id integer NOT NULL
);


ALTER TABLE public.bn_module_traces OWNER TO postgres;

--
-- Name: TABLE bn_module_traces; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_module_traces IS 'This table stores the association between modules and traces.';


--
-- Name: COLUMN bn_module_traces.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_module_traces.module_id IS 'The module id a trace belongs to.';


--
-- Name: COLUMN bn_module_traces.trace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_module_traces.trace_id IS 'The id of the trace.';


--
-- Name: bn_module_views; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_module_views (
    module_id integer NOT NULL,
    view_id integer NOT NULL
);


ALTER TABLE public.bn_module_views OWNER TO postgres;

--
-- Name: TABLE bn_module_views; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_module_views IS 'This table stores the association between modules and views.';


--
-- Name: COLUMN bn_module_views.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_module_views.module_id IS 'The module id a view belongs to.';


--
-- Name: COLUMN bn_module_views.view_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_module_views.view_id IS 'The id of the view.';


--
-- Name: bn_modules; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_modules (
    id integer NOT NULL,
    raw_module_id integer,
    name text NOT NULL,
    description text NOT NULL,
    md5 character(32) NOT NULL,
    sha1 character(40) NOT NULL,
    debugger_id integer,
    image_base bigint DEFAULT 0 NOT NULL,
    file_base bigint DEFAULT 0 NOT NULL,
    import_time timestamp without time zone DEFAULT now() NOT NULL,
    modification_date timestamp without time zone DEFAULT now() NOT NULL,
    data bytea,
    stared boolean DEFAULT false NOT NULL,
    initialization_state integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.bn_modules OWNER TO postgres;

--
-- Name: TABLE bn_modules; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_modules IS 'This table holds the information about a BinNavi module.';


--
-- Name: COLUMN bn_modules.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.id IS 'The id of the module.';


--
-- Name: COLUMN bn_modules.raw_module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.raw_module_id IS 'The id of the corresponding raw module.';


--
-- Name: COLUMN bn_modules.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.name IS 'The name of the module.';


--
-- Name: COLUMN bn_modules.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.description IS 'The description of the module.';


--
-- Name: COLUMN bn_modules.md5; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.md5 IS 'The md5 hash of the binary which corresponds to this module.';


--
-- Name: COLUMN bn_modules.sha1; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.sha1 IS 'The sha1 has of the binary which corresponds to this module.';


--
-- Name: COLUMN bn_modules.debugger_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.debugger_id IS 'The id of the debugger currently active for this module.';


--
-- Name: COLUMN bn_modules.image_base; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.image_base IS 'The image base of the executable represented by the module.';


--
-- Name: COLUMN bn_modules.file_base; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.file_base IS 'The file base of the executable represented by the module.';


--
-- Name: COLUMN bn_modules.import_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.import_time IS 'The time of import.';


--
-- Name: COLUMN bn_modules.modification_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.modification_date IS 'The time when the database was last updated.';


--
-- Name: COLUMN bn_modules.data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.data IS 'The data of binary represented by the module.';


--
-- Name: COLUMN bn_modules.stared; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.stared IS 'Flags if the module has been stared.';


--
-- Name: COLUMN bn_modules.initialization_state; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_modules.initialization_state IS 'Indicates the initialization state of the module';


--
-- Name: bn_modules_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_modules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_modules_id_seq OWNER TO postgres;

--
-- Name: bn_modules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_modules_id_seq OWNED BY bn_modules.id;


--
-- Name: SEQUENCE bn_modules_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_modules_id_seq IS 'This sequence is used in the table bn_modules id field.';


--
-- Name: bn_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_nodes (
    id integer NOT NULL,
    view_id integer NOT NULL,
    parent_id integer,
    type node_type NOT NULL,
    x double precision NOT NULL,
    y double precision NOT NULL,
    width double precision NOT NULL,
    height double precision NOT NULL,
    color integer NOT NULL,
    bordercolor integer DEFAULT 0 NOT NULL,
    selected boolean NOT NULL,
    visible boolean NOT NULL
);


ALTER TABLE public.bn_nodes OWNER TO postgres;

--
-- Name: TABLE bn_nodes; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_nodes IS 'This table holds the information representing a base node.';


--
-- Name: COLUMN bn_nodes.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.id IS 'The id of the node.';


--
-- Name: COLUMN bn_nodes.view_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.view_id IS 'The id of the view the node belongs to.';


--
-- Name: COLUMN bn_nodes.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.parent_id IS 'The potential parent node of this node in z-axis';


--
-- Name: COLUMN bn_nodes.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.type IS 'The type of the node see node_type.';


--
-- Name: COLUMN bn_nodes.x; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.x IS 'The x coordinate of the node.';


--
-- Name: COLUMN bn_nodes.y; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.y IS 'The y coordinate of the node.';


--
-- Name: COLUMN bn_nodes.width; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.width IS 'The width of the node.';


--
-- Name: COLUMN bn_nodes.height; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.height IS 'The height of the node.';


--
-- Name: COLUMN bn_nodes.color; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.color IS 'The color of the node.';


--
-- Name: COLUMN bn_nodes.bordercolor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.bordercolor IS 'The border color of the node.';


--
-- Name: COLUMN bn_nodes.selected; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.selected IS 'Flags if the node is selected.';


--
-- Name: COLUMN bn_nodes.visible; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes.visible IS 'Flags if the node is visible.';


--
-- Name: bn_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_nodes_id_seq OWNER TO postgres;

--
-- Name: bn_nodes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_nodes_id_seq OWNED BY bn_nodes.id;


--
-- Name: SEQUENCE bn_nodes_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_nodes_id_seq IS 'This sequence is used by the table bn_nodes id field.';


--
-- Name: bn_nodes_spacemodules; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_nodes_spacemodules (
    module_id integer NOT NULL,
    node integer NOT NULL,
    address_space integer
);


ALTER TABLE public.bn_nodes_spacemodules OWNER TO postgres;

--
-- Name: TABLE bn_nodes_spacemodules; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_nodes_spacemodules IS 'This table holds the information about a nodes module association when in a project.';


--
-- Name: COLUMN bn_nodes_spacemodules.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes_spacemodules.module_id IS 'The module id the node is associated to.';


--
-- Name: COLUMN bn_nodes_spacemodules.node; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes_spacemodules.node IS 'The id of the node.';


--
-- Name: COLUMN bn_nodes_spacemodules.address_space; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_nodes_spacemodules.address_space IS 'The id of the address space.';


--
-- Name: bn_operands; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_operands (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    expression_tree_id integer NOT NULL,
    "position" integer NOT NULL
);


ALTER TABLE public.bn_operands OWNER TO postgres;

--
-- Name: TABLE bn_operands; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_operands IS 'This table stores the information about operands.';


--
-- Name: COLUMN bn_operands.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_operands.module_id IS 'The module id the operand belongs to.';


--
-- Name: COLUMN bn_operands.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_operands.address IS 'The address where the operand can be found.';


--
-- Name: COLUMN bn_operands.expression_tree_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_operands.expression_tree_id IS 'The expression tree id of the operand.';


--
-- Name: COLUMN bn_operands."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_operands."position" IS 'The position of the operand.';


--
-- Name: bn_project_debuggers; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_project_debuggers (
    project_id integer NOT NULL,
    debugger_id integer NOT NULL
);


ALTER TABLE public.bn_project_debuggers OWNER TO postgres;

--
-- Name: TABLE bn_project_debuggers; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_project_debuggers IS 'This table stores the information about debuggers associated to projects.';


--
-- Name: COLUMN bn_project_debuggers.project_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_debuggers.project_id IS 'The id of a project.';


--
-- Name: COLUMN bn_project_debuggers.debugger_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_debuggers.debugger_id IS 'The id of a debugger.';


--
-- Name: bn_project_settings; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_project_settings (
    project_id integer NOT NULL,
    name character varying(255) NOT NULL,
    value text NOT NULL
);


ALTER TABLE public.bn_project_settings OWNER TO postgres;

--
-- Name: TABLE bn_project_settings; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_project_settings IS 'This table stores all settings of a project.';


--
-- Name: COLUMN bn_project_settings.project_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_settings.project_id IS 'The id of the project the setting belongs to.';


--
-- Name: COLUMN bn_project_settings.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_settings.name IS 'The name of the setting.';


--
-- Name: COLUMN bn_project_settings.value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_settings.value IS 'The value of the setting.';


--
-- Name: bn_project_traces; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_project_traces (
    project_id integer NOT NULL,
    trace_id integer NOT NULL
);


ALTER TABLE public.bn_project_traces OWNER TO postgres;

--
-- Name: TABLE bn_project_traces; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_project_traces IS 'This table stores the association between a project and a trace.';


--
-- Name: COLUMN bn_project_traces.project_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_traces.project_id IS 'The id of the project';


--
-- Name: COLUMN bn_project_traces.trace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_traces.trace_id IS 'The id of the trace.';


--
-- Name: bn_project_views; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_project_views (
    project_id integer NOT NULL,
    view_id integer NOT NULL
);


ALTER TABLE public.bn_project_views OWNER TO postgres;

--
-- Name: TABLE bn_project_views; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_project_views IS 'This table stores the association of a view to a project.';


--
-- Name: COLUMN bn_project_views.project_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_views.project_id IS 'The id of the project.';


--
-- Name: COLUMN bn_project_views.view_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_project_views.view_id IS 'The id of the view.';


--
-- Name: bn_projects; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_projects (
    id integer NOT NULL,
    name text NOT NULL,
    description text NOT NULL,
    creation_date timestamp without time zone DEFAULT now() NOT NULL,
    modification_date timestamp without time zone DEFAULT now() NOT NULL,
    stared boolean DEFAULT false NOT NULL
);


ALTER TABLE public.bn_projects OWNER TO postgres;

--
-- Name: TABLE bn_projects; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_projects IS 'This table stores all information about a project.';


--
-- Name: COLUMN bn_projects.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_projects.id IS 'The id of the project. Backed by bn_projects_id_seq';


--
-- Name: COLUMN bn_projects.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_projects.name IS 'The name of the project.';


--
-- Name: COLUMN bn_projects.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_projects.description IS 'The description of the project.';


--
-- Name: COLUMN bn_projects.creation_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_projects.creation_date IS 'The creation date of the project.';


--
-- Name: COLUMN bn_projects.modification_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_projects.modification_date IS 'The modification date of the project.';


--
-- Name: COLUMN bn_projects.stared; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_projects.stared IS 'Flag which indicates if the project is stared.';


--
-- Name: bn_projects_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_projects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_projects_id_seq OWNER TO postgres;

--
-- Name: bn_projects_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_projects_id_seq OWNED BY bn_projects.id;


--
-- Name: SEQUENCE bn_projects_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_projects_id_seq IS 'This sequence is used by the table bn_projects id field.';


--
-- Name: bn_sections; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_sections (
    module_id integer NOT NULL,
    id integer NOT NULL,
    name text NOT NULL,
    comment_id integer,
    start_address bigint NOT NULL,
    end_address bigint NOT NULL,
    permission permission_type,
    data bytea
);


ALTER TABLE public.bn_sections OWNER TO postgres;

--
-- Name: TABLE bn_sections; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_sections IS 'This table stores the information about sections known to BinNavi.';


--
-- Name: COLUMN bn_sections.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_sections.module_id IS 'The module id the section belongs to.';


--
-- Name: COLUMN bn_sections.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_sections.id IS 'The id of the section.';


--
-- Name: COLUMN bn_sections.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_sections.name IS 'The name of the section.';


--
-- Name: COLUMN bn_sections.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_sections.comment_id IS 'The id of the comment associated with this section.';


--
-- Name: COLUMN bn_sections.start_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_sections.start_address IS 'The start address of the section.';


--
-- Name: COLUMN bn_sections.end_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_sections.end_address IS 'The end address of the section.';


--
-- Name: COLUMN bn_sections.permission; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_sections.permission IS 'The permissions of the section see bn_permission_type for more information';


--
-- Name: COLUMN bn_sections.data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_sections.data IS 'The actual data of the section.';


--
-- Name: bn_sections_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_sections_id_seq OWNER TO postgres;

--
-- Name: bn_sections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_sections_id_seq OWNED BY bn_sections.id;


--
-- Name: SEQUENCE bn_sections_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_sections_id_seq IS 'This sequence is used by the table bn_sections id field.';


--
-- Name: bn_space_modules; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_space_modules (
    module_id integer NOT NULL,
    address_space_id integer NOT NULL,
    image_base bigint NOT NULL
);


ALTER TABLE public.bn_space_modules OWNER TO postgres;

--
-- Name: TABLE bn_space_modules; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_space_modules IS 'This table stores the association between modules and address spaces.';


--
-- Name: COLUMN bn_space_modules.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_space_modules.module_id IS 'The id of the module.';


--
-- Name: COLUMN bn_space_modules.address_space_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_space_modules.address_space_id IS 'The id of the address space.';


--
-- Name: COLUMN bn_space_modules.image_base; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_space_modules.image_base IS 'The image base of the module in the context of the address space.';


--
-- Name: bn_tagged_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_tagged_nodes (
    node_id integer NOT NULL,
    tag_id integer NOT NULL
);


ALTER TABLE public.bn_tagged_nodes OWNER TO postgres;

--
-- Name: TABLE bn_tagged_nodes; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_tagged_nodes IS 'This table stores the association between tags and nodes.';


--
-- Name: COLUMN bn_tagged_nodes.node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tagged_nodes.node_id IS 'The node id of a tagged node.';


--
-- Name: COLUMN bn_tagged_nodes.tag_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tagged_nodes.tag_id IS 'The id of a tag.';


--
-- Name: bn_tagged_views; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_tagged_views (
    view_id integer NOT NULL,
    tag_id integer NOT NULL
);


ALTER TABLE public.bn_tagged_views OWNER TO postgres;

--
-- Name: TABLE bn_tagged_views; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_tagged_views IS 'This table stores the association between tags and views.';


--
-- Name: COLUMN bn_tagged_views.view_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tagged_views.view_id IS 'The view id of a tagged view.';


--
-- Name: COLUMN bn_tagged_views.tag_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tagged_views.tag_id IS 'The id of a tag.';


--
-- Name: bn_tags; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_tags (
    id integer NOT NULL,
    parent_id integer,
    name text NOT NULL,
    description text NOT NULL,
    type tag_type NOT NULL
);


ALTER TABLE public.bn_tags OWNER TO postgres;

--
-- Name: TABLE bn_tags; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_tags IS 'This table stores all information about tags';


--
-- Name: COLUMN bn_tags.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tags.id IS 'The id of the tag.';


--
-- Name: COLUMN bn_tags.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tags.parent_id IS 'The id of a potential parent tag.';


--
-- Name: COLUMN bn_tags.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tags.name IS 'The name of the tag.';


--
-- Name: COLUMN bn_tags.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tags.description IS 'The description of the tag.';


--
-- Name: COLUMN bn_tags.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_tags.type IS 'The type of the tag.';


--
-- Name: bn_tags_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_tags_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_tags_id_seq OWNER TO postgres;

--
-- Name: bn_tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_tags_id_seq OWNED BY bn_tags.id;


--
-- Name: SEQUENCE bn_tags_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_tags_id_seq IS 'This sequence is used by the table bn_tags id field.';


--
-- Name: bn_text_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_text_nodes (
    node_id integer NOT NULL,
    comment_id integer
);


ALTER TABLE public.bn_text_nodes OWNER TO postgres;

--
-- Name: TABLE bn_text_nodes; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_text_nodes IS 'This table stores the information about text nodes.';


--
-- Name: COLUMN bn_text_nodes.node_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_text_nodes.node_id IS 'The node id of the text node.';


--
-- Name: COLUMN bn_text_nodes.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_text_nodes.comment_id IS 'The id of the comment associated to the text node.';


--
-- Name: bn_trace_event_values; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_trace_event_values (
    trace_id integer NOT NULL,
    "position" integer NOT NULL,
    register_name character varying(50) NOT NULL,
    register_value bigint NOT NULL,
    memory_value bytea NOT NULL
);


ALTER TABLE public.bn_trace_event_values OWNER TO postgres;

--
-- Name: TABLE bn_trace_event_values; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_trace_event_values IS 'This table holds the information about register contents for a trace event.';


--
-- Name: COLUMN bn_trace_event_values.trace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_event_values.trace_id IS 'The id of the trace the event values are associated to.';


--
-- Name: COLUMN bn_trace_event_values."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_event_values."position" IS 'The position of the values within the trace.';


--
-- Name: COLUMN bn_trace_event_values.register_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_event_values.register_name IS 'The name of the register for which we have the values.';


--
-- Name: COLUMN bn_trace_event_values.register_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_event_values.register_value IS 'The values of the register.';


--
-- Name: COLUMN bn_trace_event_values.memory_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_event_values.memory_value IS 'The value of the memory pointed to by the register when there was valid memory available.';


--
-- Name: bn_trace_events; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_trace_events (
    module_id integer,
    trace_id integer NOT NULL,
    "position" integer NOT NULL,
    tid integer NOT NULL,
    address bigint NOT NULL,
    type integer
);


ALTER TABLE public.bn_trace_events OWNER TO postgres;

--
-- Name: TABLE bn_trace_events; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_trace_events IS 'This table holds the information about a single trace event.';


--
-- Name: COLUMN bn_trace_events.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_events.module_id IS 'The id of the module the trace is associated to.';


--
-- Name: COLUMN bn_trace_events.trace_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_events.trace_id IS 'The id of the trace.';


--
-- Name: COLUMN bn_trace_events."position"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_events."position" IS 'The position of the trace event within the trace.';


--
-- Name: COLUMN bn_trace_events.tid; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_events.tid IS 'The thread id of the trace event.';


--
-- Name: COLUMN bn_trace_events.address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_events.address IS 'The address of the trace event.';


--
-- Name: COLUMN bn_trace_events.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_trace_events.type IS 'The type of the trace event.';


--
-- Name: bn_traces; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_traces (
    id integer NOT NULL,
    view_id integer NOT NULL,
    name text NOT NULL,
    description text NOT NULL
);


ALTER TABLE public.bn_traces OWNER TO postgres;

--
-- Name: TABLE bn_traces; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_traces IS 'This table holds the information about traces.';


--
-- Name: COLUMN bn_traces.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_traces.id IS 'The id of the trace.';


--
-- Name: COLUMN bn_traces.view_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_traces.view_id IS 'The view id to which the trace is associated.';


--
-- Name: COLUMN bn_traces.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_traces.name IS 'The name of the trace.';


--
-- Name: COLUMN bn_traces.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_traces.description IS 'The description of the trace.';


--
-- Name: bn_traces_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_traces_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_traces_id_seq OWNER TO postgres;

--
-- Name: bn_traces_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_traces_id_seq OWNED BY bn_traces.id;


--
-- Name: SEQUENCE bn_traces_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_traces_id_seq IS 'This sequence is used by the table bn_traces id field.';


--
-- Name: bn_type_instances; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_type_instances (
    module_id integer NOT NULL,
    id integer NOT NULL,
    name text,
    comment_id integer,
    type_id integer NOT NULL,
    section_id integer NOT NULL,
    section_offset bigint NOT NULL
);


ALTER TABLE public.bn_type_instances OWNER TO postgres;

--
-- Name: TABLE bn_type_instances; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_type_instances IS 'This table stores the information about type instances known to BinNavi.';


--
-- Name: COLUMN bn_type_instances.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_type_instances.module_id IS 'The module id the type instance belongs to.';


--
-- Name: COLUMN bn_type_instances.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_type_instances.id IS 'The id of the type instance.';


--
-- Name: COLUMN bn_type_instances.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_type_instances.name IS 'The name of the type instance.';


--
-- Name: COLUMN bn_type_instances.comment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_type_instances.comment_id IS 'The id of the comment associated with this type instance.';


--
-- Name: COLUMN bn_type_instances.type_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_type_instances.type_id IS 'The type id of the type that backs the type instance.';


--
-- Name: COLUMN bn_type_instances.section_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_type_instances.section_id IS 'The id of the section where in combination with address types value can be found.';


--
-- Name: COLUMN bn_type_instances.section_offset; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_type_instances.section_offset IS 'The offset of the type instance in the section.';


--
-- Name: bn_type_instances_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_type_instances_id_seq
    START WITH 0
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_type_instances_id_seq OWNER TO postgres;

--
-- Name: bn_type_instances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_type_instances_id_seq OWNED BY bn_type_instances.id;


--
-- Name: SEQUENCE bn_type_instances_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_type_instances_id_seq IS 'This sequence is used by the table bn_type_instances id field.';


--
-- Name: bn_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_types (
    module_id integer NOT NULL,
    id integer NOT NULL,
    name text NOT NULL,
    base_type integer NOT NULL,
    parent_id integer,
    "offset" integer,
    argument integer,
    number_of_elements integer
);


ALTER TABLE public.bn_types OWNER TO postgres;

--
-- Name: TABLE bn_types; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_types IS 'This table holds the information about concrete types.';


--
-- Name: COLUMN bn_types.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_types.module_id IS 'The id of the module the type is associated to.';


--
-- Name: COLUMN bn_types.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_types.id IS 'The id of the type.';


--
-- Name: COLUMN bn_types.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_types.name IS 'The name of the type.';


--
-- Name: COLUMN bn_types.base_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_types.base_type IS 'The base type of this type.';


--
-- Name: COLUMN bn_types.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_types.parent_id IS 'The potential parent type of this type.';


--
-- Name: COLUMN bn_types."offset"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_types."offset" IS 'Used for struct types and object types to define the offset of the member within the compound type.';


--
-- Name: COLUMN bn_types.argument; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_types.argument IS 'Used for function pointer types to indicate which argument this type is.';


--
-- Name: COLUMN bn_types.number_of_elements; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_types.number_of_elements IS 'Used for array types to indicate the number of elements in an array';


--
-- Name: bn_types_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_types_id_seq OWNER TO postgres;

--
-- Name: bn_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_types_id_seq OWNED BY bn_types.id;


--
-- Name: SEQUENCE bn_types_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_types_id_seq IS 'This sequence is used by the table bn_types id field.';


--
-- Name: bn_users; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_users (
    user_id integer NOT NULL,
    user_name text,
    user_image bytea,
    user_image_filename text
);


ALTER TABLE public.bn_users OWNER TO postgres;

--
-- Name: TABLE bn_users; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_users IS 'This table holds all information about users in BinNavi';


--
-- Name: COLUMN bn_users.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_users.user_id IS 'The id of the user.';


--
-- Name: COLUMN bn_users.user_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_users.user_name IS 'The name of the user.';


--
-- Name: COLUMN bn_users.user_image; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_users.user_image IS 'Currently unused.';


--
-- Name: COLUMN bn_users.user_image_filename; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_users.user_image_filename IS 'Currently unused.';


--
-- Name: bn_users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_users_user_id_seq OWNER TO postgres;

--
-- Name: bn_users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_users_user_id_seq OWNED BY bn_users.user_id;


--
-- Name: SEQUENCE bn_users_user_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_users_user_id_seq IS 'This sequence is used by the table bn_users id field.';


--
-- Name: bn_view_settings; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_view_settings (
    view_id integer NOT NULL,
    name character varying(255) NOT NULL,
    value text NOT NULL
);


ALTER TABLE public.bn_view_settings OWNER TO postgres;

--
-- Name: TABLE bn_view_settings; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_view_settings IS 'This table holds configuration settings for a particular view.';


--
-- Name: COLUMN bn_view_settings.view_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_view_settings.view_id IS 'The view id associated to this setting.';


--
-- Name: COLUMN bn_view_settings.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_view_settings.name IS 'The name of the setting.';


--
-- Name: COLUMN bn_view_settings.value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_view_settings.value IS 'The value of the setting.';


--
-- Name: bn_views; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE bn_views (
    id integer NOT NULL,
    type view_type NOT NULL,
    name text NOT NULL,
    description text,
    creation_date timestamp without time zone DEFAULT now() NOT NULL,
    modification_date timestamp without time zone DEFAULT now() NOT NULL,
    stared boolean DEFAULT false NOT NULL,
    user_id integer
);


ALTER TABLE public.bn_views OWNER TO postgres;

--
-- Name: TABLE bn_views; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE bn_views IS 'This table holds the information about views.';


--
-- Name: COLUMN bn_views.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_views.id IS 'The id of the view.';


--
-- Name: COLUMN bn_views.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_views.type IS 'The type of the view see view_type.';


--
-- Name: COLUMN bn_views.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_views.name IS 'The name of the view.';


--
-- Name: COLUMN bn_views.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_views.description IS 'The description of the view.';


--
-- Name: COLUMN bn_views.creation_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_views.creation_date IS 'The date when the view was originally created.';


--
-- Name: COLUMN bn_views.modification_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_views.modification_date IS 'The date when the view was last modified.';


--
-- Name: COLUMN bn_views.stared; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_views.stared IS 'Flags if the view is stared.';


--
-- Name: COLUMN bn_views.user_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN bn_views.user_id IS 'Defines the owner of the view.';


--
-- Name: bn_views_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE bn_views_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bn_views_id_seq OWNER TO postgres;

--
-- Name: bn_views_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE bn_views_id_seq OWNED BY bn_views.id;


--
-- Name: SEQUENCE bn_views_id_seq; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON SEQUENCE bn_views_id_seq IS 'This sequence is used by the table bn_views id field.';


--
-- Name: ex_1_address_comments; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_address_comments (
    address bigint NOT NULL,
    comment text NOT NULL
);


ALTER TABLE public.ex_1_address_comments OWNER TO postgres;

--
-- Name: ex_1_address_references; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_address_references (
    address bigint NOT NULL,
    "position" integer,
    expression_node_id integer,
    destination bigint NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    CONSTRAINT ex_1_address_references_type_check CHECK (((type >= 0) AND (type <= 8)))
);


ALTER TABLE public.ex_1_address_references OWNER TO postgres;

--
-- Name: ex_1_base_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_base_types (
    id integer NOT NULL,
    name text NOT NULL,
    size integer NOT NULL,
    pointer integer,
    signed boolean,
    category ex_1_type_category NOT NULL
);


ALTER TABLE public.ex_1_base_types OWNER TO postgres;

--
-- Name: ex_1_basic_block_instructions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_basic_block_instructions (
    basic_block_id integer NOT NULL,
    instruction bigint NOT NULL,
    sequence integer NOT NULL
);


ALTER TABLE public.ex_1_basic_block_instructions OWNER TO postgres;

--
-- Name: ex_1_basic_blocks; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_basic_blocks (
    id integer NOT NULL,
    parent_function bigint NOT NULL,
    address bigint NOT NULL
);


ALTER TABLE public.ex_1_basic_blocks OWNER TO postgres;

--
-- Name: ex_1_callgraph; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_callgraph (
    id integer NOT NULL,
    source bigint NOT NULL,
    source_basic_block_id integer NOT NULL,
    source_address bigint NOT NULL,
    destination bigint NOT NULL
);


ALTER TABLE public.ex_1_callgraph OWNER TO postgres;

--
-- Name: ex_1_callgraph_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_1_callgraph_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_1_callgraph_id_seq OWNER TO postgres;

--
-- Name: ex_1_callgraph_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_1_callgraph_id_seq OWNED BY ex_1_callgraph.id;


--
-- Name: ex_1_control_flow_graphs; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_control_flow_graphs (
    id integer NOT NULL,
    parent_function bigint NOT NULL,
    source integer NOT NULL,
    destination integer NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    CONSTRAINT ex_1_control_flow_graphs_type_check CHECK ((type = ANY (ARRAY[0, 1, 2, 3])))
);


ALTER TABLE public.ex_1_control_flow_graphs OWNER TO postgres;

--
-- Name: ex_1_control_flow_graphs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_1_control_flow_graphs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_1_control_flow_graphs_id_seq OWNER TO postgres;

--
-- Name: ex_1_control_flow_graphs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_1_control_flow_graphs_id_seq OWNED BY ex_1_control_flow_graphs.id;


--
-- Name: ex_1_expression_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_expression_nodes (
    id integer NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    symbol character varying(256),
    immediate bigint,
    "position" integer,
    parent_id integer,
    CONSTRAINT ex_1_expression_nodes_check CHECK ((id > parent_id)),
    CONSTRAINT ex_1_expression_nodes_type_check CHECK (((type >= 0) AND (type <= 7)))
);


ALTER TABLE public.ex_1_expression_nodes OWNER TO postgres;

--
-- Name: ex_1_expression_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_1_expression_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_1_expression_nodes_id_seq OWNER TO postgres;

--
-- Name: ex_1_expression_nodes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_1_expression_nodes_id_seq OWNED BY ex_1_expression_nodes.id;


--
-- Name: ex_1_expression_substitutions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_expression_substitutions (
    id integer NOT NULL,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_node_id integer NOT NULL,
    replacement text NOT NULL
);


ALTER TABLE public.ex_1_expression_substitutions OWNER TO postgres;

--
-- Name: ex_1_expression_substitutions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_1_expression_substitutions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_1_expression_substitutions_id_seq OWNER TO postgres;

--
-- Name: ex_1_expression_substitutions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_1_expression_substitutions_id_seq OWNED BY ex_1_expression_substitutions.id;


--
-- Name: ex_1_expression_tree_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_expression_tree_nodes (
    expression_tree_id integer NOT NULL,
    expression_node_id integer NOT NULL
);


ALTER TABLE public.ex_1_expression_tree_nodes OWNER TO postgres;

--
-- Name: ex_1_expression_trees; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_expression_trees (
    id integer NOT NULL
);


ALTER TABLE public.ex_1_expression_trees OWNER TO postgres;

--
-- Name: ex_1_expression_trees_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_1_expression_trees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_1_expression_trees_id_seq OWNER TO postgres;

--
-- Name: ex_1_expression_trees_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_1_expression_trees_id_seq OWNED BY ex_1_expression_trees.id;


--
-- Name: ex_1_expression_type_instances; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_expression_type_instances (
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_node_id integer NOT NULL,
    type_instance_id integer NOT NULL
);


ALTER TABLE public.ex_1_expression_type_instances OWNER TO postgres;

--
-- Name: ex_1_expression_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_expression_types (
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    type integer NOT NULL,
    path integer[] NOT NULL,
    "offset" integer
);


ALTER TABLE public.ex_1_expression_types OWNER TO postgres;

--
-- Name: ex_1_functions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_functions (
    address bigint NOT NULL,
    name text NOT NULL,
    demangled_name text,
    has_real_name boolean NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    module_name text,
    stack_frame integer,
    CONSTRAINT ex_1_functions_type_check CHECK ((type = ANY (ARRAY[0, 1, 2, 3, 4])))
);


ALTER TABLE public.ex_1_functions OWNER TO postgres;

--
-- Name: ex_1_instructions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_instructions (
    address bigint NOT NULL,
    mnemonic character varying(32) NOT NULL,
    data bytea NOT NULL
);


ALTER TABLE public.ex_1_instructions OWNER TO postgres;

--
-- Name: ex_1_operands; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_operands (
    address bigint NOT NULL,
    expression_tree_id integer NOT NULL,
    "position" integer NOT NULL
);


ALTER TABLE public.ex_1_operands OWNER TO postgres;

--
-- Name: ex_1_sections; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_sections (
    id integer NOT NULL,
    name text NOT NULL,
    start_address bigint NOT NULL,
    end_address bigint NOT NULL,
    permission ex_1_section_permission_type NOT NULL,
    data bytea NOT NULL
);


ALTER TABLE public.ex_1_sections OWNER TO postgres;

--
-- Name: ex_1_sections_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_1_sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_1_sections_id_seq OWNER TO postgres;

--
-- Name: ex_1_sections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_1_sections_id_seq OWNED BY ex_1_sections.id;


--
-- Name: ex_1_type_instances; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_type_instances (
    id integer NOT NULL,
    name text NOT NULL,
    section_offset bigint NOT NULL,
    type_id integer NOT NULL,
    section_id integer NOT NULL
);


ALTER TABLE public.ex_1_type_instances OWNER TO postgres;

--
-- Name: ex_1_type_renderers; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_type_renderers (
    type_id integer NOT NULL,
    renderer ex_1_type_renderers_renderer_type NOT NULL
);


ALTER TABLE public.ex_1_type_renderers OWNER TO postgres;

--
-- Name: ex_1_type_substitution_paths; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_type_substitution_paths (
    id integer NOT NULL,
    child_id integer,
    type_id integer NOT NULL
);


ALTER TABLE public.ex_1_type_substitution_paths OWNER TO postgres;

--
-- Name: ex_1_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_1_types (
    id integer NOT NULL,
    name text NOT NULL,
    base_type integer NOT NULL,
    parent_id integer,
    "offset" integer,
    argument integer,
    number_of_elements integer
);


ALTER TABLE public.ex_1_types OWNER TO postgres;

--
-- Name: ex_1_types_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_1_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_1_types_id_seq OWNER TO postgres;

--
-- Name: ex_1_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_1_types_id_seq OWNED BY ex_1_types.id;


--
-- Name: ex_2_address_comments; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_address_comments (
    address bigint NOT NULL,
    comment text NOT NULL
);


ALTER TABLE public.ex_2_address_comments OWNER TO postgres;

--
-- Name: ex_2_address_references; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_address_references (
    address bigint NOT NULL,
    "position" integer,
    expression_node_id integer,
    destination bigint NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    CONSTRAINT ex_2_address_references_type_check CHECK (((type >= 0) AND (type <= 8)))
);


ALTER TABLE public.ex_2_address_references OWNER TO postgres;

--
-- Name: ex_2_base_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_base_types (
    id integer NOT NULL,
    name text NOT NULL,
    size integer NOT NULL,
    pointer integer,
    signed boolean,
    category ex_2_type_category NOT NULL
);


ALTER TABLE public.ex_2_base_types OWNER TO postgres;

--
-- Name: ex_2_basic_block_instructions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_basic_block_instructions (
    basic_block_id integer NOT NULL,
    instruction bigint NOT NULL,
    sequence integer NOT NULL
);


ALTER TABLE public.ex_2_basic_block_instructions OWNER TO postgres;

--
-- Name: ex_2_basic_blocks; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_basic_blocks (
    id integer NOT NULL,
    parent_function bigint NOT NULL,
    address bigint NOT NULL
);


ALTER TABLE public.ex_2_basic_blocks OWNER TO postgres;

--
-- Name: ex_2_callgraph; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_callgraph (
    id integer NOT NULL,
    source bigint NOT NULL,
    source_basic_block_id integer NOT NULL,
    source_address bigint NOT NULL,
    destination bigint NOT NULL
);


ALTER TABLE public.ex_2_callgraph OWNER TO postgres;

--
-- Name: ex_2_callgraph_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_2_callgraph_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_2_callgraph_id_seq OWNER TO postgres;

--
-- Name: ex_2_callgraph_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_2_callgraph_id_seq OWNED BY ex_2_callgraph.id;


--
-- Name: ex_2_control_flow_graphs; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_control_flow_graphs (
    id integer NOT NULL,
    parent_function bigint NOT NULL,
    source integer NOT NULL,
    destination integer NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    CONSTRAINT ex_2_control_flow_graphs_type_check CHECK ((type = ANY (ARRAY[0, 1, 2, 3])))
);


ALTER TABLE public.ex_2_control_flow_graphs OWNER TO postgres;

--
-- Name: ex_2_control_flow_graphs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_2_control_flow_graphs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_2_control_flow_graphs_id_seq OWNER TO postgres;

--
-- Name: ex_2_control_flow_graphs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_2_control_flow_graphs_id_seq OWNED BY ex_2_control_flow_graphs.id;


--
-- Name: ex_2_expression_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_expression_nodes (
    id integer NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    symbol character varying(256),
    immediate bigint,
    "position" integer,
    parent_id integer,
    CONSTRAINT ex_2_expression_nodes_check CHECK ((id > parent_id)),
    CONSTRAINT ex_2_expression_nodes_type_check CHECK (((type >= 0) AND (type <= 7)))
);


ALTER TABLE public.ex_2_expression_nodes OWNER TO postgres;

--
-- Name: ex_2_expression_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_2_expression_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_2_expression_nodes_id_seq OWNER TO postgres;

--
-- Name: ex_2_expression_nodes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_2_expression_nodes_id_seq OWNED BY ex_2_expression_nodes.id;


--
-- Name: ex_2_expression_substitutions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_expression_substitutions (
    id integer NOT NULL,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_node_id integer NOT NULL,
    replacement text NOT NULL
);


ALTER TABLE public.ex_2_expression_substitutions OWNER TO postgres;

--
-- Name: ex_2_expression_substitutions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_2_expression_substitutions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_2_expression_substitutions_id_seq OWNER TO postgres;

--
-- Name: ex_2_expression_substitutions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_2_expression_substitutions_id_seq OWNED BY ex_2_expression_substitutions.id;


--
-- Name: ex_2_expression_tree_nodes; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_expression_tree_nodes (
    expression_tree_id integer NOT NULL,
    expression_node_id integer NOT NULL
);


ALTER TABLE public.ex_2_expression_tree_nodes OWNER TO postgres;

--
-- Name: ex_2_expression_trees; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_expression_trees (
    id integer NOT NULL
);


ALTER TABLE public.ex_2_expression_trees OWNER TO postgres;

--
-- Name: ex_2_expression_trees_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_2_expression_trees_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_2_expression_trees_id_seq OWNER TO postgres;

--
-- Name: ex_2_expression_trees_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_2_expression_trees_id_seq OWNED BY ex_2_expression_trees.id;


--
-- Name: ex_2_expression_type_instances; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_expression_type_instances (
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_node_id integer NOT NULL,
    type_instance_id integer NOT NULL
);


ALTER TABLE public.ex_2_expression_type_instances OWNER TO postgres;

--
-- Name: ex_2_expression_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_expression_types (
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    type integer NOT NULL,
    path integer[] NOT NULL,
    "offset" integer
);


ALTER TABLE public.ex_2_expression_types OWNER TO postgres;

--
-- Name: ex_2_functions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_functions (
    address bigint NOT NULL,
    name text NOT NULL,
    demangled_name text,
    has_real_name boolean NOT NULL,
    type integer DEFAULT 0 NOT NULL,
    module_name text,
    stack_frame integer,
    CONSTRAINT ex_2_functions_type_check CHECK ((type = ANY (ARRAY[0, 1, 2, 3, 4])))
);


ALTER TABLE public.ex_2_functions OWNER TO postgres;

--
-- Name: ex_2_instructions; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_instructions (
    address bigint NOT NULL,
    mnemonic character varying(32) NOT NULL,
    data bytea NOT NULL
);


ALTER TABLE public.ex_2_instructions OWNER TO postgres;

--
-- Name: ex_2_operands; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_operands (
    address bigint NOT NULL,
    expression_tree_id integer NOT NULL,
    "position" integer NOT NULL
);


ALTER TABLE public.ex_2_operands OWNER TO postgres;

--
-- Name: ex_2_sections; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_sections (
    id integer NOT NULL,
    name text NOT NULL,
    start_address bigint NOT NULL,
    end_address bigint NOT NULL,
    permission ex_2_section_permission_type NOT NULL,
    data bytea NOT NULL
);


ALTER TABLE public.ex_2_sections OWNER TO postgres;

--
-- Name: ex_2_sections_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_2_sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_2_sections_id_seq OWNER TO postgres;

--
-- Name: ex_2_sections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_2_sections_id_seq OWNED BY ex_2_sections.id;


--
-- Name: ex_2_type_instances; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_type_instances (
    id integer NOT NULL,
    name text NOT NULL,
    section_offset bigint NOT NULL,
    type_id integer NOT NULL,
    section_id integer NOT NULL
);


ALTER TABLE public.ex_2_type_instances OWNER TO postgres;

--
-- Name: ex_2_type_renderers; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_type_renderers (
    type_id integer NOT NULL,
    renderer ex_2_type_renderers_renderer_type NOT NULL
);


ALTER TABLE public.ex_2_type_renderers OWNER TO postgres;

--
-- Name: ex_2_type_substitution_paths; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_type_substitution_paths (
    id integer NOT NULL,
    child_id integer,
    type_id integer NOT NULL
);


ALTER TABLE public.ex_2_type_substitution_paths OWNER TO postgres;

--
-- Name: ex_2_types; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE ex_2_types (
    id integer NOT NULL,
    name text NOT NULL,
    base_type integer NOT NULL,
    parent_id integer,
    "offset" integer,
    argument integer,
    number_of_elements integer
);


ALTER TABLE public.ex_2_types OWNER TO postgres;

--
-- Name: ex_2_types_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE ex_2_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ex_2_types_id_seq OWNER TO postgres;

--
-- Name: ex_2_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE ex_2_types_id_seq OWNED BY ex_2_types.id;


--
-- Name: modules; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE modules (
    id integer NOT NULL,
    name text NOT NULL,
    architecture character varying(32) NOT NULL,
    base_address bigint NOT NULL,
    exporter character varying(256) NOT NULL,
    version integer NOT NULL,
    md5 character(32) NOT NULL,
    sha1 character(40) NOT NULL,
    comment text,
    import_time timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.modules OWNER TO postgres;

--
-- Name: modules_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE modules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.modules_id_seq OWNER TO postgres;

--
-- Name: modules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE modules_id_seq OWNED BY modules.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_address_spaces ALTER COLUMN id SET DEFAULT nextval('bn_address_spaces_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_debuggers ALTER COLUMN id SET DEFAULT nextval('bn_debuggers_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_edges ALTER COLUMN id SET DEFAULT nextval('bn_edges_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_modules ALTER COLUMN id SET DEFAULT nextval('bn_modules_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_nodes ALTER COLUMN id SET DEFAULT nextval('bn_nodes_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_projects ALTER COLUMN id SET DEFAULT nextval('bn_projects_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_sections ALTER COLUMN id SET DEFAULT nextval('bn_sections_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_tags ALTER COLUMN id SET DEFAULT nextval('bn_tags_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_traces ALTER COLUMN id SET DEFAULT nextval('bn_traces_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_type_instances ALTER COLUMN id SET DEFAULT nextval('bn_type_instances_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_types ALTER COLUMN id SET DEFAULT nextval('bn_types_id_seq'::regclass);


--
-- Name: user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_users ALTER COLUMN user_id SET DEFAULT nextval('bn_users_user_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY bn_views ALTER COLUMN id SET DEFAULT nextval('bn_views_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_callgraph ALTER COLUMN id SET DEFAULT nextval('ex_1_callgraph_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_control_flow_graphs ALTER COLUMN id SET DEFAULT nextval('ex_1_control_flow_graphs_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_nodes ALTER COLUMN id SET DEFAULT nextval('ex_1_expression_nodes_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_substitutions ALTER COLUMN id SET DEFAULT nextval('ex_1_expression_substitutions_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_expression_trees ALTER COLUMN id SET DEFAULT nextval('ex_1_expression_trees_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_sections ALTER COLUMN id SET DEFAULT nextval('ex_1_sections_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_1_types ALTER COLUMN id SET DEFAULT nextval('ex_1_types_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_callgraph ALTER COLUMN id SET DEFAULT nextval('ex_2_callgraph_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_control_flow_graphs ALTER COLUMN id SET DEFAULT nextval('ex_2_control_flow_graphs_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_nodes ALTER COLUMN id SET DEFAULT nextval('ex_2_expression_nodes_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_substitutions ALTER COLUMN id SET DEFAULT nextval('ex_2_expression_substitutions_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_expression_trees ALTER COLUMN id SET DEFAULT nextval('ex_2_expression_trees_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_sections ALTER COLUMN id SET DEFAULT nextval('ex_2_sections_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY ex_2_types ALTER COLUMN id SET DEFAULT nextval('ex_2_types_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY modules ALTER COLUMN id SET DEFAULT nextval('modules_id_seq'::regclass);