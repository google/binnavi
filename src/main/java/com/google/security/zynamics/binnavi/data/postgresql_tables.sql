SET check_function_bodies = false;
SET default_tablespace = '';
SET default_with_oids = false;

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;
-- COMMENT ON EXTENSION plpgsql
--   IS 'PL/pgSQL procedural language';

--
-- Types section
--

--
-- address_reference_type
--

DROP TYPE IF EXISTS address_reference_type CASCADE;
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
COMMENT ON TYPE address_reference_type
  IS 'The address_reference_type defines all possible address reference types.';

--
-- architecture_type
--

DROP TYPE IF EXISTS architecture_type CASCADE;
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
COMMENT ON TYPE architecture_type
  IS 'The architecture_type defines all architectures known to BinNavi.
  Unknown architectures used the generic type.';

--
-- edge_type
--

DROP TYPE IF EXISTS edge_type CASCADE;
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
COMMENT ON TYPE edge_type
  IS 'The edge_type defines all possible types of an edge.
  This type is used in BinNavi to enable specific functions for an edge.';

--
-- function_type
--

DROP TYPE IF EXISTS function_type CASCADE;
CREATE TYPE function_type AS ENUM (
    'normal',
    'library',
    'import',
    'thunk',
    'adjustor_thunk',
    'invalid'
);
COMMENT ON TYPE function_type
  IS 'The function_type defines all possible function types.
  This type is used in BinNavi to enable specific functions for a function.';

--
-- node_type
--

DROP TYPE IF EXISTS node_type CASCADE;
CREATE TYPE node_type AS ENUM (
    'code',
    'function',
    'group',
    'text'
);
COMMENT ON TYPE node_type
  IS 'The node_type defines all possible node type.
  The type is used in BinNavi to enable specific functions for a node.';

--
-- permission type
--

DROP TYPE IF EXISTS permission_type CASCADE;
CREATE TYPE permission_type AS ENUM (
  'READ',
  'WRITE',
  'EXECUTE',
  'READ_WRITE',
  'READ_EXECUTE',
  'READ_WRITE_EXECUTE',
  'WRITE_EXECUTE'
);
COMMENT ON TYPE permission_type
  IS 'The permission_type is used to describe the permission a section has.
  This information can either come from the exporter or from the debugger.';

--
-- tag_type
--

DROP TYPE IF EXISTS tag_type CASCADE;
CREATE TYPE tag_type AS ENUM (
    'view_tag',
    'node_tag'
);
COMMENT ON TYPE tag_type
  IS 'The tag_type defines to which taggable instance the tags belongs.
  A tag can either be a view tag or a node tag.';

--
-- view_type
--

DROP TYPE IF EXISTS view_type CASCADE;
CREATE TYPE view_type AS ENUM (
    'native',
    'non-native'
);
COMMENT ON TYPE view_type
  IS 'The view_type defines where a view comes from.
  Native views come from the export / disassembler.
  Non-native views have been generated in BinNavi.
  Native views are immutable.';

--
-- type_cagetory
--

DROP TYPE IF EXISTS type_category CASCADE;
CREATE TYPE type_category AS ENUM (
  'atomic',
  'array',
  'pointer',
  'struct',
  'union',
  'function_pointer'
);
COMMENT ON TYPE type_category
  IS 'The type_category enum specifies the category of a given base type as defined in a C type system.';

--
-- Begin trigger section.
--

--
-- bn_sections_trigger()
--

CREATE OR REPLACE FUNCTION bn_sections_trigger()
  RETURNS trigger AS
$$
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
$$ LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION bn_sections_trigger()
  IS 'The bn_sections_trigger is called for all altering operations on the bn_sections table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi';

--
-- bn_types_trigger()
--

CREATE OR REPLACE FUNCTION bn_types_trigger()
  RETURNS trigger AS
$$
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
$$
  LANGUAGE plpgsql VOLATILE COST 100;
COMMENT ON FUNCTION bn_types_trigger() IS 'The bn_types_trigger is called for all altering operations on the bn_types table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi';

--
-- bn_base_types_trigger()
--

CREATE OR REPLACE FUNCTION bn_base_types_trigger()
  RETURNS trigger AS
$$
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
$$
  LANGUAGE plpgsql VOLATILE COST 100;
COMMENT ON FUNCTION bn_base_types_trigger() IS 'The bn_base_types_trigger is called for all altering operations on the bn_base_types table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi';

--
-- bn_type_instances_trigger()
--

CREATE OR REPLACE FUNCTION bn_type_instances_trigger()
  RETURNS trigger AS
$BODY$
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
$BODY$
  LANGUAGE plpgsql VOLATILE COST 100;
COMMENT ON FUNCTION bn_type_instances_trigger() IS 'The bn_expression_type_instances_trigger is called for all altering operations on the bn_expression_type_instances table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi.';

--
-- bn_type_instances_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_type_instances_comment_trigger()
  RETURNS trigger AS
$$
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
$$
  LANGUAGE plpgsql VOLATILE COST 100;
COMMENT ON FUNCTION bn_type_instances_comment_trigger() IS 'The bn_type_instances_comment_trigger is called for UPDATE operations on the bn_type_instances table an will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to type instances between multiple instances of BinNavi.';

--
-- bn_expression_types_trigger()
--

CREATE OR REPLACE FUNCTION bn_expression_types_trigger()
  RETURNS trigger AS
$$
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
$$
  LANGUAGE plpgsql VOLATILE COST 100;
COMMENT ON FUNCTION bn_expression_types_trigger() IS 'The bn_expression_types_trigger is called for all altering operations on the bn_expression_types table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronization between multiple instances of BinNavi';

--
-- bn_ecpression_type_instances_trigger()
--

CREATE OR REPLACE FUNCTION bn_expression_type_instances_trigger()
  RETURNS trigger AS
$BODY$
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
$BODY$
  LANGUAGE plpgsql VOLATILE COST 100;
COMMENT ON FUNCTION bn_expression_type_instances_trigger() IS 'The bn_expression_type_instances_trigger is called for all operations on the bn_expression_type_instances table an will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronization of type instances associated to type instances between multiple instances of BinNavi.';

--
-- bn_functions_trigger()
--

CREATE OR REPLACE FUNCTION bn_functions_trigger()
  RETURNS trigger AS
$$
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
$$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION bn_functions_trigger()
  IS 'The bn_functions_trigger is called for all altering operations on the bn_functions table and will perform a pg_notify with the altered information.
  This information will be used in BinNavi to provide synchronisation between multiple instances of BinNavi';

--
-- bn_module_views_trigger()
--

CREATE OR REPLACE FUNCTION bn_module_views_trigger()
  RETURNS trigger AS
$$
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
$$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION bn_module_views_trigger()
  IS 'The bn_module_views_trigger is called for all altering operations on the bn_module_views table and will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of module views between multiple instances of BinNavi.';

--
-- bn_project_views_trigger()
--

CREATE OR REPLACE FUNCTION bn_project_views_trigger()
  RETURNS trigger AS
$$
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
$$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION bn_project_views_trigger()
  IS 'The bn_project_views_trigger is called for all altering operations on the bn_project_views table and will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of project views between multiple instances of BinNavi.';

--
-- bn_views_trigger()
--

CREATE OR REPLACE FUNCTION bn_views_trigger()
  RETURNS trigger AS
$$
 BEGIN
  IF ( TG_OP = 'UPDATE' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || NEW.id );
    RETURN NEW;
  ELSIF ( TG_OP = 'DELETE' ) THEN
    PERFORM pg_notify('view_changes', TG_TABLE_NAME || ' ' || TG_OP || ' ' || OLD.id );
    RETURN OLD;
  END IF;
 END;
 $$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION bn_views_trigger()
  IS 'The bn_views_trigger is called for UPDATE and DELETE operations on the bn_views table and will perform a pg_notify with the altered information.
  This infromation is used in BinNavi to provide synchronisation of views between multiple instances of BinNavi.';

--
-- bn_comments_trigger()
--

CREATE OR REPLACE FUNCTION bn_comments_trigger()
  RETURNS trigger
  LANGUAGE plpgsql
  AS
$$
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
COMMENT ON FUNCTION bn_comments_trigger()
  IS 'The bn_comments_trigger is called for UPDATE and DELETE operations on the bn_comments table and will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments between multiple instances of BinNavi.';

--
-- bn_code_node_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_code_node_comment_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_code_node_comment_trigger()
  IS 'The bn_bn_code_node_comment_trigger is called for UPDATE operations on the bn_code_nodes table an will perform a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of local comments associated to code nodes between multiple instances of BinNavi.';

--
-- bn_codenode_instructions_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_codenode_instructions_comment_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_codenode_instructions_comment_trigger()
  IS 'The bn_codenode_instructions_comment_trigger is called for UPDATE operations on the bn_codenode_instructions table and will perform a pg_notify with the altered information
  This information is used in BinNavi to provide synchronisation of local comments associated to instructions between multiple instances of BinNavi.';

--
-- bn_comments_audit_logger()
--

CREATE OR REPLACE FUNCTION bn_comments_audit_logger() RETURNS trigger
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
COMMENT ON FUNCTION bn_comments_audit_logger()
  IS 'The bn_comments_audit_logger is called for all operations performed on the bn_comments table and saves the operation with the altered information in the table
  bn_comments_audit. This information can be used to track changes to comments which have been performed to a database over time.';

--
-- bn_edges_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_edges_comment_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_edges_comment_trigger()
  IS 'The bn_edges_comment_trigger is called for UPDATE operations on the bn_edges table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of local comments associated to edges between multiple instances of BinNavi.';

--
-- bn_function_nodes_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_function_nodes_comment_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_function_nodes_comment_trigger()
  IS 'The bn_function_nodes_comment_trigger is called for UPDATE operations on the bn_function_nodes table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to function nodes between multiple instances of BinNavi.';

--
-- bn_functions_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_functions_comment_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_functions_comment_trigger()
  IS 'The bn_functions_comment_trigger is called for UPDATE operations on the bn_function table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to functions between multiple instances of BinNavi.';

--
-- bn_global_edge_comments_trigger()
--

CREATE OR REPLACE FUNCTION bn_global_edge_comments_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_global_edge_comments_trigger()
  IS 'The bn_global_edge_comments_trigger is called for all operations on the bn_global_edge_comments table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of global comments associated to edges between multiple instances of BinNavi.';

--
-- bn_global_node_comments_trigger()
--

CREATE OR REPLACE FUNCTION bn_global_node_comments_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_global_node_comments_trigger()
  IS 'The bn_global_node_comments_trigger is called for all operations on the bn_global_node_comments table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of global comments associated to nodes between multiple instances of BinNavi.';

--
-- bn_group_nodes_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_group_nodes_comment_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_group_nodes_comment_trigger()
  IS 'The bn_group_nodes_comment_trigger is called for all operations on the bn_group_nodes table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to group nodes between multiple instances of BinNavi.';

--
-- bn_instructions_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_instructions_comment_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_instructions_comment_trigger()
  IS 'The bn_instructions_comment_trigger is called for UPDATE operations on the bn_instructions table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of global comments associated to instructions between multiple instances of BinNavi.';

--
-- bn_text_nodes_comment_trigger()
--

CREATE OR REPLACE FUNCTION bn_text_nodes_comment_trigger() RETURNS trigger
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
COMMENT ON FUNCTION bn_text_nodes_comment_trigger()
  IS 'The bn_text_nodes_comment_trigger is called for all operations on the bn_text_nodes table and will perfrom a pg_notify with the altered information.
  This information is used in BinNavi to provide synchronisation of comments associated to text nodes between multiple instances of BinNavi.';

--
-- Table section
--

--
-- bn_debuggers
--

CREATE SEQUENCE bn_debuggers_id_seq;
COMMENT ON SEQUENCE bn_debuggers_id_seq
    IS 'This sequence is used by the table bn_debuggers id field.';

CREATE TABLE bn_debuggers (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_debuggers_id_seq'::regclass),
    name text NOT NULL,
    host text NOT NULL,
    port integer NOT NULL
);

COMMENT ON TABLE bn_debuggers IS 'This table contains all information to connect to a debug client.';
COMMENT ON COLUMN bn_debuggers.id IS 'Id of the debug client.';
COMMENT ON COLUMN bn_debuggers.name IS 'Name of the debug client.';
COMMENT ON COLUMN bn_debuggers.host IS 'Host name of the debug client.';
COMMENT ON COLUMN bn_debuggers.port IS 'Port number of the debug client.';

ALTER SEQUENCE bn_debuggers_id_seq OWNED BY bn_debuggers.id;

--
-- bn_modules
--

CREATE SEQUENCE bn_modules_id_seq;
COMMENT ON SEQUENCE bn_modules_id_seq
    IS 'This sequence is used in the table bn_modules id field.';

CREATE TABLE bn_modules (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_modules_id_seq'::regclass),
    raw_module_id integer, --REFERENCES modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    name text NOT NULL,
    description text NOT NULL,
    md5 character(32) NOT NULL,
    sha1 character(40) NOT NULL,
    debugger_id integer REFERENCES bn_debuggers(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    image_base bigint DEFAULT 0 NOT NULL,
    file_base bigint DEFAULT 0 NOT NULL,
    import_time timestamp without time zone DEFAULT now() NOT NULL,
    modification_date timestamp without time zone DEFAULT now() NOT NULL,
    data bytea,
    stared boolean DEFAULT false NOT NULL,
    initialization_state integer DEFAULT 0 NOT NULL
);

COMMENT ON TABLE bn_modules IS 'This table holds the information about a BinNavi module.';
COMMENT ON COLUMN bn_modules.id IS 'The id of the module.';
COMMENT ON COLUMN bn_modules.raw_module_id IS 'The id of the corresponding raw module.';
COMMENT ON COLUMN bn_modules.name IS 'The name of the module.';
COMMENT ON COLUMN bn_modules.description IS 'The description of the module.';
COMMENT ON COLUMN bn_modules.md5 IS 'The md5 hash of the binary which corresponds to this module.';
COMMENT ON COLUMN bn_modules.sha1 IS 'The sha1 has of the binary which corresponds to this module.';
COMMENT ON COLUMN bn_modules.debugger_id IS 'The id of the debugger currently active for this module.';
COMMENT ON COLUMN bn_modules.image_base IS 'The image base of the executable represented by the module.';
COMMENT ON COLUMN bn_modules.file_base IS 'The file base of the executable represented by the module.';
COMMENT ON COLUMN bn_modules.import_time IS 'The time of import.';
COMMENT ON COLUMN bn_modules.modification_date IS 'The time when the database was last updated.';
COMMENT ON COLUMN bn_modules.data IS 'The data of binary represented by the module.';
COMMENT ON COLUMN bn_modules.stared IS 'Flags if the module has been stared.';
COMMENT ON COLUMN bn_modules.initialization_state IS 'Indicates the initialization state of the module';

ALTER SEQUENCE bn_modules_id_seq OWNED BY bn_modules.id;

CREATE INDEX bn_modules_raw_module_id_idx
    ON bn_modules USING btree (raw_module_id);

--
-- bn_address_references
--

CREATE TABLE bn_address_references (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    type address_reference_type NOT NULL,
    target bigint NOT NULL,
	CONSTRAINT bn_address_references_pkey PRIMARY KEY (module_id, address, "position", expression_id, type, target)
);

COMMENT ON TABLE bn_address_references IS 'This table stores all address references.';
COMMENT ON COLUMN bn_address_references.module_id IS 'The module id the address reference is associated to.';
COMMENT ON COLUMN bn_address_references.address IS 'The address where the reference is associated to.';
COMMENT ON COLUMN bn_address_references."position" IS 'The position of the operand tree to which the address reference is associated to.';
COMMENT ON COLUMN bn_address_references.expression_id IS 'The id of the expression in the operand tree the address reference is associated to.';
COMMENT ON COLUMN bn_address_references.type IS 'The type of the address reference see the address_reference_type type for details.';
COMMENT ON COLUMN bn_address_references.target IS 'The target where address reference points to.';

CREATE INDEX bn_address_references_module_id_address_position_expression_id_
  ON bn_address_references USING btree (module_id, address, "position", expression_id);

CREATE INDEX bn_address_references_module_id_idx
  ON bn_address_references USING btree (module_id);

CREATE INDEX bn_address_references_target_idx
  ON bn_address_references USING btree (target);

CREATE INDEX bn_address_references_type_idx
  ON bn_address_references USING btree (type);

--
-- bn_projects
--

CREATE SEQUENCE bn_projects_id_seq;
COMMENT ON SEQUENCE bn_projects_id_seq
    IS 'This sequence is used by the table bn_projects id field.';

CREATE TABLE bn_projects (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_projects_id_seq'::regclass),
    name text NOT NULL,
    description text NOT NULL,
    creation_date timestamp without time zone DEFAULT now() NOT NULL,
    modification_date timestamp without time zone DEFAULT now() NOT NULL,
    stared boolean DEFAULT false NOT NULL
);

COMMENT ON TABLE bn_projects IS 'This table stores all information about a project.';
COMMENT ON COLUMN bn_projects.id IS 'The id of the project. Backed by bn_projects_id_seq';
COMMENT ON COLUMN bn_projects.name IS 'The name of the project.';
COMMENT ON COLUMN bn_projects.description IS 'The description of the project.';
COMMENT ON COLUMN bn_projects.creation_date IS 'The creation date of the project.';
COMMENT ON COLUMN bn_projects.modification_date IS 'The modification date of the project.';
COMMENT ON COLUMN bn_projects.stared IS 'Flag which indicates if the project is stared.';

ALTER SEQUENCE bn_projects_id_seq OWNED BY bn_projects.id;

--
-- bn_address_spaces
--

CREATE SEQUENCE bn_address_spaces_id_seq;
COMMENT ON SEQUENCE bn_address_spaces_id_seq
    IS 'This sequence is used by the table bn_address_spaces id field.';

CREATE TABLE bn_address_spaces (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_address_spaces_id_seq'::regclass),
    project_id integer NOT NULL REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    debugger_id integer REFERENCES bn_debuggers(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    name text NOT NULL,
    description text NOT NULL,
    creation_date timestamp without time zone DEFAULT now() NOT NULL,
    modification_date timestamp without time zone DEFAULT now() NOT NULL
);

COMMENT ON TABLE bn_address_spaces IS 'This table stores the information about address spaces associated to a project.';
COMMENT ON COLUMN bn_address_spaces.id IS 'The id of the address space which is backed by the sequence bn_address_spaces_id_seq';
COMMENT ON COLUMN bn_address_spaces.project_id IS 'The id of the project the address space is associated to.';
COMMENT ON COLUMN bn_address_spaces.debugger_id IS 'The id of the current debugger of the address space.';
COMMENT ON COLUMN bn_address_spaces.name IS 'The name of the address space.';
COMMENT ON COLUMN bn_address_spaces.description IS 'The description of the address space.';
COMMENT ON COLUMN bn_address_spaces.creation_date IS 'The creation date of the address space.';
COMMENT ON COLUMN bn_address_spaces.modification_date IS 'The modification date of the address space.';

ALTER SEQUENCE bn_address_spaces_id_seq OWNED BY bn_address_spaces.id;

--
-- bn_base_types
--

CREATE SEQUENCE bn_base_types_id_seq;
COMMENT ON SEQUENCE bn_base_types_id_seq
    IS 'This sequence is used by the table bn_base_types id field.';

CREATE TABLE bn_base_types (
    module_id integer NOT NULL,
    id integer NOT NULL,
    name text NOT NULL,
    size integer NOT NULL,
    pointer integer,
    signed boolean,
    category type_category NOT NULL,
    CONSTRAINT bn_base_types_pkey PRIMARY KEY (module_id, id),
    CONSTRAINT bn_base_types_pointer_fkey
        FOREIGN KEY (module_id, pointer)
        REFERENCES bn_base_types(module_id, id)
        ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_base_types IS 'This table stores all base types for the type system used in BinNavi.';
COMMENT ON COLUMN bn_base_types.module_id IS 'The module id the base type is associated to.';
COMMENT ON COLUMN bn_base_types.id IS 'The id of the base type.';
COMMENT ON COLUMN bn_base_types.name IS 'The name of the base type.';
COMMENT ON COLUMN bn_base_types.size IS 'The size of the base type in bits.';
COMMENT ON COLUMN bn_base_types.pointer IS 'A flag that indicates if the base type is a pointer or not.';
COMMENT ON COLUMN bn_base_types.signed IS 'A flag that indicates if the base type id signed or not.';
COMMENT ON COLUMN bn_base_types.category IS 'An enum that describes the category of this base type.';

ALTER SEQUENCE bn_base_types_id_seq OWNED BY bn_base_types.id;

CREATE TRIGGER bn_base_types_trigger
  AFTER INSERT OR UPDATE OR DELETE
  ON bn_base_types
  FOR EACH ROW EXECUTE
    PROCEDURE bn_base_types_trigger();
--
-- bn_nodes
--

CREATE SEQUENCE bn_nodes_id_seq;
COMMENT ON SEQUENCE bn_nodes_id_seq
    IS 'This sequence is used by the table bn_nodes id field.';

CREATE TABLE bn_nodes (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_nodes_id_seq'::regclass),
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

COMMENT ON TABLE bn_nodes IS 'This table holds the information representing a base node.';
COMMENT ON COLUMN bn_nodes.id IS 'The id of the node.';
COMMENT ON COLUMN bn_nodes.view_id IS 'The id of the view the node belongs to.';
COMMENT ON COLUMN bn_nodes.parent_id IS 'The potential parent node of this node in z-axis';
COMMENT ON COLUMN bn_nodes.type IS 'The type of the node see node_type.';
COMMENT ON COLUMN bn_nodes.x IS 'The x coordinate of the node.';
COMMENT ON COLUMN bn_nodes.y IS 'The y coordinate of the node.';
COMMENT ON COLUMN bn_nodes.width IS 'The width of the node.';
COMMENT ON COLUMN bn_nodes.height IS 'The height of the node.';
COMMENT ON COLUMN bn_nodes.color IS 'The color of the node.';
COMMENT ON COLUMN bn_nodes.bordercolor IS 'The border color of the node.';
COMMENT ON COLUMN bn_nodes.selected IS 'Flags if the node is selected.';
COMMENT ON COLUMN bn_nodes.visible IS 'Flags if the node is visible.';

ALTER SEQUENCE bn_nodes_id_seq OWNED BY bn_nodes.id;

CREATE INDEX bn_nodes_type
  ON bn_nodes USING btree (type);

CREATE INDEX bn_nodes_view_id
  ON bn_nodes USING btree (view_id);

CREATE INDEX bn_nodes_view_id_type_idx
  ON bn_nodes USING btree (view_id, type);

--
-- bn_users
--

CREATE SEQUENCE bn_users_user_id_seq;
COMMENT ON SEQUENCE bn_users_user_id_seq
  IS 'This sequence is used by the table bn_users id field.';

CREATE TABLE bn_users (
    user_id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_users_user_id_seq'::regclass),
    user_name text,
    user_image bytea,
    user_image_filename text
);

COMMENT ON TABLE bn_users IS 'This table holds all information about users in BinNavi';
COMMENT ON COLUMN bn_users.user_id IS 'The id of the user.';
COMMENT ON COLUMN bn_users.user_name IS 'The name of the user.';
COMMENT ON COLUMN bn_users.user_image IS 'Currently unused.';
COMMENT ON COLUMN bn_users.user_image_filename IS 'Currently unused.';

ALTER SEQUENCE bn_users_user_id_seq OWNED BY bn_users.user_id;

--
-- bn_comments
--

CREATE SEQUENCE bn_comments_id_seq;
COMMENT ON SEQUENCE bn_comments_id_seq
  IS 'This sequence is used by the table bn_comments id field.';

CREATE TABLE bn_comments (
    id integer NOT NULL PRIMARY KEY,
    parent_id integer REFERENCES bn_comments(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED,
    user_id integer NOT NULL REFERENCES bn_users(user_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    comment_text text NOT NULL
);

COMMENT ON TABLE bn_comments IS 'This table stores all information about comments';
COMMENT ON COLUMN bn_comments.id IS 'The id of the comment used in all other tables as reference to this table.';
COMMENT ON COLUMN bn_comments.parent_id IS 'This column contains the id of the comment which is the parent of this comment.nThe idea here is that the latest generated comment will be refered to by the comment_id in the tables having a comment such that we will traverse the comments upwards to generate the complete comment stream ';
COMMENT ON COLUMN bn_comments.user_id IS 'The owner of the comment which is the only person to be able to delete or edit this comment.nThis value does not provide any security it is just there to reduce concurrent modification problems.';
COMMENT ON COLUMN bn_comments.comment_text IS 'The actual comment.';

ALTER SEQUENCE bn_comments_id_seq OWNED BY bn_comments.id;

CREATE INDEX bn_comments_parent_id_idx
  ON bn_comments USING btree (parent_id);

CREATE INDEX bn_comments_user_id_idx
  ON bn_comments USING btree (user_id);

CREATE TRIGGER bn_comments_audit_trigger
  AFTER INSERT OR DELETE OR UPDATE ON bn_comments
  FOR EACH ROW
    EXECUTE PROCEDURE bn_comments_audit_logger();

CREATE TRIGGER bn_comments_trigger
  AFTER UPDATE OR DELETE
  ON bn_comments
  FOR EACH ROW
    EXECUTE PROCEDURE bn_comments_trigger();

--
-- bn_code_nodes
--

CREATE TABLE bn_code_nodes (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    node_id integer NOT NULL PRIMARY KEY REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    parent_function bigint,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_code_nodes IS 'This table stores all information about code nodes.';
COMMENT ON COLUMN bn_code_nodes.module_id IS 'The module id the code node belongs to.';
COMMENT ON COLUMN bn_code_nodes.node_id IS 'The node id the code node is associated with.';
COMMENT ON COLUMN bn_code_nodes.parent_function IS 'The parent function of the code node.';
COMMENT ON COLUMN bn_code_nodes.comment_id IS 'The id of the comment associacted with the code node.';


CREATE INDEX bn_code_nodes_module_id_idx
    ON bn_code_nodes USING btree (module_id);

CREATE INDEX bn_code_nodes_comment_id_idx
    ON bn_code_nodes USING btree (comment_id);

CREATE TRIGGER bn_code_node_comment_trigger
    AFTER UPDATE OF comment_id ON bn_code_nodes
    FOR EACH ROW EXECUTE
        PROCEDURE bn_code_node_comment_trigger();

--
-- bn_codenode_instructions
--

CREATE TABLE bn_codenode_instructions (
    module_id integer NOT NULL,
    node_id integer NOT NULL REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    "position" integer NOT NULL,
    address bigint NOT NULL,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_codenode_instructions_pkey PRIMARY KEY (node_id, "position")
);

COMMENT ON TABLE bn_codenode_instructions IS 'This table stores the association between instructions and code nodes.';
COMMENT ON COLUMN bn_codenode_instructions.module_id IS 'The module id the association between instruction and code node belongs to.';
COMMENT ON COLUMN bn_codenode_instructions.node_id IS 'The node id to which the instruction is associated.';
COMMENT ON COLUMN bn_codenode_instructions.position IS 'The position within the node the instruction has.';
COMMENT ON COLUMN bn_codenode_instructions.address IS 'The address of the code node.';
COMMENT ON COLUMN bn_codenode_instructions.comment_id IS 'The id of the comment associated with the instruction in the code node.';

CREATE INDEX bn_codenode_instructions_address_idx
  ON bn_codenode_instructions USING btree (address);

CREATE INDEX bn_codenode_instructions_module_id_address_idx
  ON bn_codenode_instructions USING btree (module_id, address);

CREATE INDEX bn_codenode_instructions_comment_id_idx
  ON bn_codenode_instructions USING btree (comment_id);

CREATE TRIGGER bn_codenode_instructions_comment_trigger
  AFTER UPDATE OF comment_id ON bn_codenode_instructions
  FOR EACH ROW EXECUTE
    PROCEDURE bn_codenode_instructions_comment_trigger();

--
-- bn_comments_audit
--

CREATE TABLE bn_comments_audit (
    operation bpchar NOT NULL,
    time_stamp timestamp with time zone DEFAULT ('now'::text)::date NOT NULL,
    id integer NOT NULL,
    parent_id integer,
    user_id integer NOT NULL,
    comment_text text NOT NULL,
    CONSTRAINT bn_comments_audit_pkey PRIMARY KEY (operation, time_stamp, id)
);

COMMENT ON TABLE bn_comments_audit IS 'This table contains all operations that have been performed on the table bn_comments.nIts purpose is that for all operations done by multiple clients on the table bn_comments there will be a log of thier activity which can help debug issues if something goes wrong.';
COMMENT ON COLUMN bn_comments_audit.operation IS 'The operation that has been performed where: U is update, I is insert and D is delete.';
COMMENT ON COLUMN bn_comments_audit.time_stamp IS 'The time stamp of the operation such that it is possible to find out when a speciffic event has occured.';
COMMENT ON COLUMN bn_comments_audit.id IS 'see bn_comments.id for description.';
COMMENT ON COLUMN bn_comments_audit.parent_id IS 'see bn_comments.parent_id for description.';
COMMENT ON COLUMN bn_comments_audit.user_id IS 'see bn_comments.user_id for description.';
COMMENT ON COLUMN bn_comments_audit.comment_text IS 'see bn_comments.comment_text for description.';

--
-- bn_data_parts
--

CREATE TABLE bn_data_parts (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    part_id integer NOT NULL,
    data bytea,
	PRIMARY KEY (module_id, part_id)
);

COMMENT ON TABLE bn_data_parts IS 'This table is used to store the original binary data of the module in the database.';
COMMENT ON COLUMN bn_data_parts.module_id IS 'Module id of a data part.';
COMMENT ON COLUMN bn_data_parts.part_id IS 'Id of a data part.';
COMMENT ON COLUMN bn_data_parts.data IS 'The actual data of a data part.';

--
-- bn_edges
--
CREATE SEQUENCE bn_edges_id_seq;
COMMENT ON SEQUENCE bn_edges_id_seq
    IS 'This sequence is used by the table bn_edges id field.';

CREATE TABLE bn_edges (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_edges_id_seq'::regclass),
    source_node_id integer NOT NULL REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    target_node_id integer NOT NULL REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    x1 double precision NOT NULL,
    y1 double precision NOT NULL,
    x2 double precision NOT NULL,
    y2 double precision NOT NULL,
    type edge_type NOT NULL,
    color integer NOT NULL,
    visible boolean NOT NULL,
    selected boolean NOT NULL,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_edges IS 'This table stores information about edges.';
COMMENT ON COLUMN bn_edges.id IS 'The id of the edge Globally unique in a single database.';
COMMENT ON COLUMN bn_edges.source_node_id IS 'The id of the node where the edge originates from.';
COMMENT ON COLUMN bn_edges.target_node_id IS 'The id of the node where the edge destined to.';
COMMENT ON COLUMN bn_edges.x1 IS 'The x1 coordinate of the edge.';
COMMENT ON COLUMN bn_edges.y1 IS 'The y1 coordinate of the edge.';
COMMENT ON COLUMN bn_edges.x2 IS 'The x2 coordinate of the edge.';
COMMENT ON COLUMN bn_edges.y2 IS 'The y2 coordinate of the edge.';
COMMENT ON COLUMN bn_edges.type IS 'The type of the edge see edge_type for all possible cases.';
COMMENT ON COLUMN bn_edges.color IS 'The color of the edge.';
COMMENT ON COLUMN bn_edges.visible IS 'Flags if the edge is currently visible or not.';
COMMENT On COLUMN bn_edges.selected IS 'Flags if the edge is currently selected or not.';
COMMENT ON COLUMN bn_edges.comment_id IS 'The id of the last comment in the comment list associated to the edge.';

ALTER SEQUENCE bn_edges_id_seq OWNED BY bn_edges.id;

CREATE INDEX bn_edges_source_node_id_idx
    ON bn_edges USING btree (source_node_id);

CREATE INDEX bn_edges_target_node_id_idx
    ON bn_edges USING btree (target_node_id);

CREATE INDEX bn_edges_comment_id_idx
    ON bn_edges USING btree (comment_id);

CREATE TRIGGER bn_edges_comment_trigger
    AFTER UPDATE OF comment_id ON bn_edges
    FOR EACH ROW
        EXECUTE PROCEDURE bn_edges_comment_trigger();

--
-- bn_edge_paths
--

CREATE TABLE bn_edge_paths (
    edge_id integer NOT NULL REFERENCES bn_edges(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    "position" integer NOT NULL,
    x double precision NOT NULL,
    y double precision NOT NULL,
	CONSTRAINT bn_edge_paths_pkey PRIMARY KEY (edge_id, "position")
);

COMMENT ON TABLE bn_edge_paths IS 'This table stores the layout information of edges.';
COMMENT ON COLUMN bn_edge_paths.edge_id IS 'The id of the edge which the path information belongs to.';
COMMENT ON COLUMN bn_edge_paths."position" IS 'The position of the edge path.';
COMMENT ON COLUMN bn_edge_paths.x IS 'The x coordinate of the edge path';
COMMENT ON COLUMN bn_edge_paths.y IS 'The y coordinate of the edge path';

--
-- bn_expression_substitutions
--

CREATE TABLE bn_expression_substitutions (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    replacement text NOT NULL,
	CONSTRAINT bn_expression_substitutions_pkey PRIMARY KEY (module_id, address, "position", expression_id)
);

COMMENT ON TABLE bn_expression_substitutions IS 'This table defines the expression substitutions.';
COMMENT ON COLUMN bn_expression_substitutions.module_id IS 'The id of the module to which this expression substitution belongs.';
COMMENT ON COLUMN bn_expression_substitutions.address IS 'The address of the expression substitution.';
COMMENT ON COLUMN bn_expression_substitutions."position" IS 'The position of the expression substitution in regards to the operands.';
COMMENT ON COLUMN bn_expression_substitutions.expression_id IS 'The id of the expression to be substituted.';
COMMENT ON COLUMN bn_expression_substitutions.replacement IS 'The text replacement for this expression substitution.';

CREATE INDEX bn_expression_substitutions_module_id_idx
  ON bn_expression_substitutions USING btree (module_id);

--
-- bn_expression_tree
--

CREATE TABLE bn_expression_tree (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    id integer NOT NULL,
    type integer NOT NULL,
    symbol character varying(256),
    immediate bigint,
    "position" integer NOT NULL,
    parent_id integer,
	CONSTRAINT bn_expression_tree_pkey PRIMARY KEY (module_id, id)
);

COMMENT ON TABLE bn_expression_tree IS 'This table defines the in BinNavi used expression trees';
COMMENT ON COLUMN bn_expression_tree.module_id IS 'The id of the module tio which the expression tree belongs.';
COMMENT ON COLUMN bn_expression_tree.id IS 'The id of the expression tree.';
COMMENT ON COLUMN bn_expression_tree.type IS 'The type of the expression tree.';
COMMENT ON COLUMN bn_expression_tree.symbol IS 'If the type is a symbol the string is saved here.';
COMMENT ON COLUMN bn_expression_tree.immediate IS 'If the type is an immediate the immediate is saved here.';
COMMENT ON COLUMN bn_expression_tree."position" IS 'The position of the expression tree.';
COMMENT ON COLUMN bn_expression_tree.parent_id IS 'If the tree has a parent tree id it is saved here.';

--
-- bn_expression_tree_ids
--

CREATE TABLE bn_expression_tree_ids (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    id integer NOT NULL,
	CONSTRAINT bn_expression_tree_ids_pkey PRIMARY KEY (module_id, id)
);

COMMENT ON TABLE bn_expression_tree_ids IS 'This table links expression tree ids to module ids.';
COMMENT ON COLUMN bn_expression_tree_ids.module_id IS 'Module id.';
COMMENT ON COLUMN bn_expression_tree_ids.id IS 'Expression tree id.';

CREATE INDEX bn_expression_tree_ids_module_id_idx
  ON bn_expression_tree_ids USING btree (module_id);

--
-- bn_expression_tree_mapping
--

CREATE TABLE bn_expression_tree_mapping (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    tree_id integer NOT NULL,
    tree_node_id integer NOT NULL,
	CONSTRAINT bn_expression_tree_mapping_pkey PRIMARY KEY (module_id, tree_id, tree_node_id)
);

COMMENT ON TABLE bn_expression_tree_mapping IS 'This table maps a tree id of an expression tree to a tree node id of an expression tree.';
COMMENT ON COLUMN bn_expression_tree_mapping.module_id IS 'The module id of the mapping.';
COMMENT ON COLUMN bn_expression_tree_mapping.tree_id IS 'The tree id of the mapping.';
COMMENT ON COLUMN bn_expression_tree_mapping.tree_node_id IS 'The tree node id of the mapping.';

CREATE INDEX bn_expression_tree_mapping_module_id_idx
  ON bn_expression_tree_mapping USING btree (module_id);

--
-- bn_types
--

CREATE SEQUENCE bn_types_id_seq;
COMMENT ON SEQUENCE bn_types_id_seq
    IS 'This sequence is used by the table bn_types id field.';

CREATE TABLE bn_types (
    module_id integer NOT NULL,
    id integer NOT NULL DEFAULT nextval('bn_types_id_seq'::regclass),
    name text NOT NULL,
    base_type integer NOT NULL,
    parent_id integer,
    "offset" integer,
    argument integer,
    number_of_elements integer,
    CONSTRAINT bn_types_pkey PRIMARY KEY (module_id, id),
    CONSTRAINT bn_types_base_type_fkey
        FOREIGN KEY (module_id, base_type)
        REFERENCES bn_base_types(module_id, id)
        ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT bn_types_parent_id_fkey
        FOREIGN KEY (module_id, parent_id)
        REFERENCES bn_base_types(module_id, id)
        ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_types IS 'This table holds the information about concrete types.';
COMMENT ON COLUMN bn_types.module_id IS 'The id of the module the type is associated to.';
COMMENT ON COLUMN bn_types.id IS 'The id of the type.';
COMMENT ON COLUMN bn_types.name IS 'The name of the type.';
COMMENT ON COLUMN bn_types.base_type IS 'The base type of this type.';
COMMENT ON COLUMN bn_types.parent_id IS 'The potential parent type of this type.';
COMMENT ON COLUMN bn_types."offset" IS 'Used for struct types and object types to define the offset of the member within the compound type.';
COMMENT ON COLUMN bn_types.argument IS 'Used for function pointer types to indicate which argument this type is.';
COMMENT ON COLUMN bn_types.number_of_elements IS 'Used for array types to indicate the number of elements in an array';

ALTER SEQUENCE bn_types_id_seq OWNED BY bn_types.id;

CREATE TRIGGER bn_types_trigger
  AFTER INSERT OR UPDATE OR DELETE
  ON bn_types
  FOR EACH ROW EXECUTE
    PROCEDURE bn_types_trigger();

--
-- bn_expression_types
--

CREATE TABLE bn_expression_types (
    module_id integer NOT NULL,
    address bigint NOT NULL,
    "position" integer NOT NULL,
    expression_id integer NOT NULL,
    base_type_id integer NOT NULL,
    path integer[],
    "offset" integer,
    PRIMARY KEY (module_id, address, "position", expression_id),
    CONSTRAINT bn_expression_types_module_id_member_id_fkey
        FOREIGN KEY (module_id, base_type_id)
        REFERENCES bn_base_types(module_id, id)
        ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_expression_types IS 'This table stores the type from the type system for a specific operand.';
COMMENT ON COLUMN bn_expression_types.module_id IS 'The module id the type is associated with.';
COMMENT ON COLUMN bn_expression_types.address IS 'The address where the type association is located.';
COMMENT ON COLUMN bn_expression_types."position" IS 'The position or the operand tree in the instruction where the type is associated to.';
COMMENT ON COLUMN bn_expression_types.expression_id IS 'The expression tree id the type is associated to.';
COMMENT ON COLUMN bn_expression_types.base_type_id IS 'The bn_base_types type which is associated here.';
COMMENT ON COLUMN bn_expression_types.path IS 'The path of the type substitution. Each integer here is an element from bn_types.';
COMMENT ON COLUMN bn_expression_types."offset" IS 'The offset of the type substitution.';

CREATE TRIGGER bn_expression_types_trigger
  AFTER INSERT OR UPDATE OR DELETE
  ON bn_expression_types
  FOR EACH ROW EXECUTE
    PROCEDURE bn_expression_types_trigger();

--
-- bn_sections
--

CREATE SEQUENCE bn_sections_id_seq;
COMMENT ON SEQUENCE bn_sections_id_seq
    IS 'This sequence is used by the table bn_sections id field.';

CREATE TABLE bn_sections (
    module_id integer NOT NULL REFERENCES bn_modules (id) ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    id integer NOT NULL DEFAULT nextval('bn_sections_id_seq'::regclass),
    name text NOT NULL,
    comment_id integer REFERENCES bn_comments (id) ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
    start_address bigint NOT NULL,
    end_address bigint NOT NULL,
    permission permission_type,
    data bytea,
    CONSTRAINT bn_sections_pkey PRIMARY KEY(module_id, id)
);

ALTER SEQUENCE bn_sections_id_seq OWNED BY bn_sections.id;

COMMENT ON TABLE bn_sections IS 'This table stores the information about sections known to BinNavi.';
COMMENT ON COLUMN bn_sections.module_id IS 'The module id the section belongs to.';
COMMENT ON COLUMN bn_sections.id IS 'The id of the section.';
COMMENT ON COLUMN bn_sections.name IS 'The name of the section.';
COMMENT ON COLUMN bn_sections.comment_id IS 'The id of the comment associated with this section.';
COMMENT ON COLUMN bn_sections.start_address IS 'The start address of the section.';
COMMENT ON COLUMN bn_sections.end_address IS 'The end address of the section.';
COMMENT ON COLUMN bn_sections.permission IS 'The permissions of the section see bn_permission_type for more information';
COMMENT ON COLUMN bn_sections.data IS 'The actual data of the section.';

CREATE INDEX bn_sections_comment_id_idx
  ON bn_sections
  USING btree
  (comment_id );

CREATE TRIGGER bn_sections_trigger
  AFTER INSERT OR DELETE OR UPDATE ON bn_sections
  FOR EACH ROW EXECUTE
    PROCEDURE bn_sections_trigger();

--
-- bn_type_instances
--


CREATE SEQUENCE bn_type_instances_id_seq START 0 MINVALUE 0;
COMMENT ON SEQUENCE bn_type_instances_id_seq
    IS 'This sequence is used by the table bn_type_instances id field.';

CREATE TABLE bn_type_instances (
    module_id integer NOT NULL REFERENCES bn_modules (id) ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    id integer NOT NULL DEFAULT nextval('bn_type_instances_id_seq'::regclass),
    name text,
    comment_id integer REFERENCES bn_comments (id) ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
    type_id integer NOT NULL,
    section_id integer NOT NULL,
    section_offset bigint NOT NULL,
    CONSTRAINT bn_type_instances_pkey PRIMARY KEY (module_id, id),
	CONSTRAINT bn_type_instances_module_id_type_id_fkey FOREIGN KEY (module_id, type_id)
        REFERENCES bn_base_types (module_id, id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT bn_type_instances_module_id_section_id_fkey FOREIGN KEY (module_id, section_id)
        REFERENCES bn_sections (module_id, id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

ALTER SEQUENCE bn_type_instances_id_seq OWNED BY bn_type_instances.id;

COMMENT ON TABLE bn_type_instances IS 'This table stores the information about type instances known to BinNavi.';
COMMENT ON COLUMN bn_type_instances.module_id IS 'The module id the type instance belongs to.';
COMMENT ON COLUMN bn_type_instances.id IS 'The id of the type instance.';
COMMENT ON COLUMN bn_type_instances.name IS 'The name of the type instance.';
COMMENT ON COLUMN bn_type_instances.comment_id IS 'The id of the comment associated with this type instance.';
COMMENT ON COLUMN bn_type_instances.type_id IS 'The type id of the type that backs the type instance.';
COMMENT ON COLUMN bn_type_instances.section_id IS 'The id of the section where in combination with address types value can be found.';
COMMENT ON COLUMN bn_type_instances.section_offset IS 'The offset of the type instance in the section.';

CREATE INDEX bn_type_instances_comment_id_idx
  ON bn_type_instances
  USING btree
  (comment_id );

CREATE TRIGGER bn_type_instances_trigger
  AFTER INSERT OR DELETE OR UPDATE ON bn_type_instances
  FOR EACH ROW EXECUTE
    PROCEDURE bn_type_instances_trigger();

CREATE TRIGGER bn_type_instances_comment_trigger
  AFTER UPDATE OF comment_id
  ON bn_type_instances
  FOR EACH ROW EXECUTE
    PROCEDURE bn_type_instances_comment_trigger();



--
-- bn_expression_type_instances
--

CREATE TABLE bn_expression_type_instances (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address bigint NOT NULL,
    position integer NOT NULL,
    expression_id integer NOT NULL,
    type_instance_id integer NOT NULL,
    PRIMARY KEY(module_id, address, position, expression_id),
    CONSTRAINT bn_expression_type_instances_module_id_type_id_fkey FOREIGN KEY (module_id, type_instance_id)
        REFERENCES bn_type_instances (module_id, id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);
COMMENT ON TABLE bn_expression_type_instances IS 'This table stores the information about data cross references. It provides the link between a type instance and an operand tree expression in the graph.';
COMMENT ON COLUMN bn_expression_type_instances.module_id IS 'The module id of the module this data xref belongs to.';
COMMENT ON COLUMN bn_expression_type_instances.address IS 'The address of the instruction to which the type instance substitution belongs.';
COMMENT ON COLUMN bn_expression_type_instances.position IS 'The position of the operand within the instruction the type instance substitution belongs to.';
COMMENT ON COLUMN bn_expression_type_instances.expression_id IS 'The expression tree id in the operand the type instance belongs to.';
COMMENT ON COLUMN bn_expression_type_instances.type_instance_id IS 'The type instance to which this substitution points.';

CREATE TRIGGER bn_expression_type_instances_trigger
  AFTER INSERT OR DELETE OR UPDATE ON bn_expression_type_instances
  FOR EACH ROW EXECUTE
    PROCEDURE bn_expression_type_instances_trigger();

--
-- bn_function_nodes
--

CREATE TABLE bn_function_nodes (
    module_id integer NOT NULL,
    node_id integer NOT NULL PRIMARY KEY REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    function bigint NOT NULL,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_function_nodes IS 'This table holds the information about function nodes in a module.';
COMMENT ON COLUMN bn_function_nodes.module_id IS 'The module id of the function node.';
COMMENT ON COLUMN bn_function_nodes.node_id IS 'The node id the function node is associated to.';
COMMENT ON COLUMN bn_function_nodes.function IS 'The function address the function node is associated to.';
COMMENT ON COLUMN bn_function_nodes.comment_id IS 'The id of the comment associated to the function node.';

CREATE INDEX bn_function_nodes_function_idx
  ON bn_function_nodes USING btree (function);

CREATE INDEX bn_function_nodes_module_id_function_idx
  ON bn_function_nodes USING btree (module_id, function);

CREATE INDEX bn_function_nodes_module_id_idx
  ON bn_function_nodes USING btree (module_id);

CREATE INDEX bn_function_nodes_comment_id_idx
  ON bn_function_nodes USING btree (comment_id);

CREATE TRIGGER bn_function_nodes_comment_trigger
  AFTER UPDATE OF comment_id ON bn_function_nodes
  FOR EACH ROW
    EXECUTE PROCEDURE bn_function_nodes_comment_trigger();

--
-- bn_views
--
CREATE SEQUENCE bn_views_id_seq;
COMMENT ON SEQUENCE bn_views_id_seq
    IS 'This sequence is used by the table bn_views id field.';

CREATE TABLE bn_views (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_views_id_seq'::regclass),
    type view_type NOT NULL,
    name text NOT NULL,
    description text,
    creation_date timestamp without time zone DEFAULT now() NOT NULL,
    modification_date timestamp without time zone DEFAULT now() NOT NULL,
    stared boolean DEFAULT false NOT NULL,
    user_id integer REFERENCES bn_users(user_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_views IS 'This table holds the information about views.';
COMMENT ON COLUMN bn_views.id IS 'The id of the view.';
COMMENT ON COLUMN bn_views.type IS 'The type of the view see view_type.';
COMMENT ON COLUMN bn_views.name IS 'The name of the view.';
COMMENT ON COLUMN bn_views.description IS 'The description of the view.';
COMMENT ON COLUMN bn_views.creation_date IS 'The date when the view was originally created.';
COMMENT ON COLUMN bn_views.modification_date IS 'The date when the view was last modified.';
COMMENT ON COLUMN bn_views.stared IS 'Flags if the view is stared.';
COMMENT ON COLUMN bn_views.user_id IS 'Defines the owner of the view.';

ALTER SEQUENCE bn_views_id_seq OWNED BY bn_views.id;

CREATE INDEX bn_views_user_id_idx
  ON bn_views USING btree (user_id);

CREATE INDEX bn_views_type_idx
  ON bn_views USING btree (type);

CREATE TRIGGER bn_views_trigger
  AFTER DELETE OR UPDATE ON bn_views
  FOR EACH ROW EXECUTE
    PROCEDURE bn_views_trigger();

--
-- bn_function_views
--

CREATE TABLE bn_function_views (
    module_id integer NOT NULL,
    view_id integer NOT NULL PRIMARY KEY REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    function bigint NOT NULL
);

COMMENT ON TABLE bn_function_views IS 'This table holds the information about function views.';
COMMENT ON COLUMN bn_function_views.module_id IS 'The module id the function view is associated to.';
COMMENT ON COLUMN bn_function_views.view_id IS 'The view id the function view is associated to.';
COMMENT ON COLUMN bn_function_views.function IS 'The address of the function the function view is associated to.';

CREATE INDEX bn_function_views_module_id_function_idx
  ON bn_function_views USING btree (module_id, function);

CREATE INDEX bn_function_views_module_id_idx
  ON bn_function_views USING btree (module_id);

--
-- bn_functions
--

CREATE TABLE bn_functions (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address bigint NOT NULL,
    name text,
    original_name text NOT NULL,
    type function_type NOT NULL,
    description text,
    parent_module_name text,
    parent_module_id integer,
    parent_module_function integer,
    stack_frame integer,
    prototype integer,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_functions_pkey PRIMARY KEY (module_id, address)
);

COMMENT ON TABLE bn_functions IS 'This table holds the information about functions and thier relations.';
COMMENT ON COLUMN bn_functions.module_id IS 'The id of the module the function belongs to.';
COMMENT ON COLUMN bn_functions.address IS 'The address of the function.';
COMMENT ON COLUMN bn_functions.name IS 'The current name of the function.';
COMMENT ON COLUMN bn_functions.original_name IS 'The original name of the function.';
COMMENT ON COLUMN bn_functions.type IS 'The type of the function see the function_type type for more information.';
COMMENT ON COLUMN bn_functions.description IS 'The description of the function.';
COMMENT ON COLUMN bn_functions.parent_module_name IS 'If the function is forwarded the module name of the function where this function is forwarded to.';
COMMENT ON COLUMN bn_functions.parent_module_id IS 'If the function is forwarded the module id of the function where this function is forwarded to.';
COMMENT ON COLUMN bn_functions.parent_module_function IS 'If the function is forwarded the address of the function where this function is forwarded to.';
COMMENT ON COLUMN bn_functions.stack_frame IS 'The bn_base_types id of the stack frame that is associated with the function.';
COMMENT ON COLUMN bn_functions.prototype IS 'The bn_base_types id of the prototype that is associated with the functions.';
COMMENT ON COLUMN bn_functions.comment_id IS 'The id of the comment associated with the function.';

CREATE INDEX bn_functions_address_idx
  ON bn_functions USING btree (address);

CREATE INDEX bn_functions_module_id_address_idx
  ON bn_functions USING btree (module_id, address);

CREATE INDEX bn_functions_module_id_address_type_idx
  ON bn_functions USING btree (module_id, address, type);

CREATE INDEX bn_functions_module_id_idx
  ON bn_functions USING btree (module_id);

CREATE INDEX bn_functions_parent_module_id_parent_module_function_idx
  ON bn_functions USING btree (parent_module_id, parent_module_function);

CREATE INDEX bn_functions_comment_id_idx
  ON bn_functions USING btree (comment_id);

CREATE TRIGGER bn_functions_comment_trigger
  AFTER UPDATE OF comment_id ON bn_functions
  FOR EACH ROW
    EXECUTE PROCEDURE bn_functions_comment_trigger();

--
-- bn_global_edge_comments
--

CREATE TABLE bn_global_edge_comments (
    src_module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    dst_module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    src_address bigint NOT NULL,
    dst_address bigint NOT NULL,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_global_edge_comments_pkey PRIMARY KEY (src_module_id, dst_module_id, src_address, dst_address)
);

COMMENT ON TABLE bn_global_edge_comments IS 'This table holds all global edge comments.';
COMMENT ON COLUMN bn_global_edge_comments.src_module_id IS 'The module id of the module where the edge originates from.';
COMMENT ON COLUMN bn_global_edge_comments.dst_module_id IS 'The module if of the module where the edge destined to.';
COMMENT ON COLUMN bn_global_edge_comments.src_address IS 'The address of the source node of the edge.';
COMMENT ON COLUMN bn_global_edge_comments.dst_address IS 'The address of the destination node of the edge.';
COMMENT ON COLUMN bn_global_edge_comments.comment_id IS 'The id of the global comment';

CREATE INDEX bn_global_edge_comments_src_module_id_idx
  ON bn_global_edge_comments USING btree (src_module_id);

CREATE INDEX bn_global_edge_comments_dst_module_id_idx
  ON bn_global_edge_comments USING btree (dst_module_id);

CREATE INDEX bn_global_edge_comments_comment_id_idx
  ON bn_global_edge_comments USING btree (comment_id);

CREATE TRIGGER bn_global_edge_comments_trigger
  AFTER INSERT OR DELETE OR UPDATE ON bn_global_edge_comments
  FOR EACH ROW
    EXECUTE PROCEDURE bn_global_edge_comments_trigger();

--
-- bn_global_node_comments
--

CREATE TABLE bn_global_node_comments (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address bigint NOT NULL,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_global_node_comments_pkey PRIMARY KEY (module_id, address)
);

COMMENT ON TABLE bn_global_node_comments IS 'This table holds all global node comments.';
COMMENT ON COLUMN bn_global_node_comments.module_id IS 'The module id of the node.';
COMMENT ON COLUMN bn_global_node_comments.address IS 'The address of the node.';
COMMENT ON COLUMN bn_global_node_comments.comment_id IS 'The id of the global node comment.';

CREATE INDEX bn_global_node_comments_comment_id_idx
  ON bn_global_node_comments USING btree (comment_id);

CREATE TRIGGER bn_global_node_comments_trigger
  AFTER INSERT OR DELETE OR UPDATE ON bn_global_node_comments
  FOR EACH ROW
    EXECUTE PROCEDURE bn_global_node_comments_trigger();

--
-- bn_group_nodes
--

CREATE TABLE bn_group_nodes (
    node_id integer NOT NULL PRIMARY KEY REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    collapsed boolean NOT NULL,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_group_nodes IS 'This table holds the information about group nodes.';
COMMENT ON COLUMN bn_group_nodes.node_id IS 'The node id of the group node.';
COMMENT ON COLUMN bn_group_nodes.collapsed IS 'Flag that indicates if the node is collapsed.';
COMMENT ON COLUMN bn_group_nodes.comment_id IS 'The id of the comment associated to the group node.';

CREATE INDEX bn_group_nodes_comments_id_idx
  ON bn_group_nodes USING btree (comment_id);

CREATE TRIGGER bn_group_nodes_comment_trigger
  AFTER INSERT OR DELETE OR UPDATE ON bn_group_nodes
  FOR EACH ROW
    EXECUTE PROCEDURE bn_group_nodes_comment_trigger();

--
-- bn_instructions
--

CREATE TABLE bn_instructions (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address bigint NOT NULL,
    mnemonic character varying(32) NOT NULL,
    data bytea NOT NULL,
    native boolean NOT NULL,
    architecture architecture_type NOT NULL,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_instructions_pkey PRIMARY KEY (module_id, address)
);

COMMENT ON TABLE bn_instructions IS 'This table holds the information about instruction.';
COMMENT ON COLUMN bn_instructions.module_id IS 'The module of the instruction.';
COMMENT ON COLUMN bn_instructions.address IS 'The address of the instruction.';
COMMENT ON COLUMN bn_instructions.mnemonic IS 'The mnemonic of the instruction.';
COMMENT ON COLUMN bn_instructions.data IS 'The raw bytes of the instruction from the binary.';
COMMENT ON COLUMN bn_instructions.native IS 'Flag that indicates if the instruction has been build within BinNavi or came from external sources.';
COMMENT ON COLUMN bn_instructions.architecture IS 'The architecture of the instruction for more information about known architecture types see srchitecture_type type.';
COMMENT ON COLUMN bn_instructions.comment_id IS 'The id of the comment associated to the instruction.';

CREATE INDEX bn_instructions_comment_id_idx
  ON bn_instructions USING btree (comment_id);

CREATE TRIGGER bn_instructions_comment_trigger
  AFTER UPDATE OF comment_id ON bn_instructions
  FOR EACH ROW
    EXECUTE PROCEDURE bn_instructions_comment_trigger();

--
-- bn_module_settings
--

CREATE TABLE bn_module_settings (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    name character varying(255) NOT NULL,
    value text NOT NULL,
	CONSTRAINT bn_module_settings_pkey PRIMARY KEY (module_id, name)
);

COMMENT ON TABLE bn_module_settings IS 'This table stores various settings for modules.';
COMMENT ON COLUMN bn_module_settings.module_id IS 'The module id of the setting.';
COMMENT ON COLUMN bn_module_settings.name IS 'The name of the setting.';
COMMENT ON COLUMN bn_module_settings.value IS 'The value of the setting.';



--
-- bn_traces
--

CREATE SEQUENCE bn_traces_id_seq;
COMMENT ON SEQUENCE bn_traces_id_seq
    IS 'This sequence is used by the table bn_traces id field.';

CREATE TABLE bn_traces (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_traces_id_seq'::regclass),
    view_id integer NOT NULL,
    name text NOT NULL,
    description text NOT NULL
);

COMMENT ON TABLE bn_traces IS 'This table holds the information about traces.';
COMMENT ON COLUMN bn_traces.id IS 'The id of the trace.';
COMMENT ON COLUMN bn_traces.view_id IS 'The view id to which the trace is associated.';
COMMENT ON COLUMN bn_traces.name IS 'The name of the trace.';
COMMENT ON COLUMN bn_traces.description IS 'The description of the trace.';

ALTER SEQUENCE bn_traces_id_seq OWNED BY bn_traces.id;

CREATE INDEX bn_traces_view_id_idx
    ON bn_traces USING btree (view_id);

--
-- bn_trace_events
--

CREATE TABLE bn_trace_events (
    module_id integer REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    trace_id integer NOT NULL REFERENCES bn_traces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    "position" integer NOT NULL,
    tid integer NOT NULL,
    address bigint NOT NULL,
    type integer,
    CONSTRAINT bn_trace_events_pkey PRIMARY KEY (trace_id, "position")
);

COMMENT ON TABLE bn_trace_events IS 'This table holds the information about a single trace event.';
COMMENT ON COLUMN bn_trace_events.module_id IS 'The id of the module the trace is associated to.';
COMMENT ON COLUMN bn_trace_events.trace_id IS 'The id of the trace.';
COMMENT ON COLUMN bn_trace_events."position" IS 'The position of the trace event within the trace.';
COMMENT ON COLUMN bn_trace_events.tid IS 'The thread id of the trace event.';
COMMENT ON COLUMN bn_trace_events.address IS 'The address of the trace event.';
COMMENT ON COLUMN bn_trace_events.type IS 'The type of the trace event.';

CREATE INDEX bn_trace_events_module_id_idx
    ON bn_trace_events USING btree (module_id);

--
-- bn_module_traces
--

CREATE TABLE bn_module_traces (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    trace_id integer NOT NULL REFERENCES bn_traces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_module_traces_pkey PRIMARY KEY (module_id, trace_id)
);

COMMENT ON TABLE bn_module_traces IS 'This table stores the association between modules and traces.';
COMMENT ON COLUMN bn_module_traces.module_id IS 'The module id a trace belongs to.';
COMMENT ON COLUMN bn_module_traces.trace_id IS 'The id of the trace.';

--
-- bn_module_views
--

CREATE TABLE bn_module_views (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    view_id integer NOT NULL PRIMARY KEY REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_module_views IS 'This table stores the association between modules and views.';
COMMENT ON COLUMN bn_module_views.module_id IS 'The module id a view belongs to.';
COMMENT ON COLUMN bn_module_views.view_id IS 'The id of the view.';

CREATE INDEX bn_module_views_module_id_view_id_idx
    ON bn_module_views USING btree (module_id, view_id);

CREATE INDEX bn_module_views_module_id_idx
    ON bn_module_views USING btree (module_id);

CREATE TRIGGER bn_module_views_trigger
    AFTER INSERT OR UPDATE OR DELETE
    ON bn_module_views
    FOR EACH ROW
        EXECUTE PROCEDURE bn_module_views_trigger();

--
-- bn_nodes_spacemodules
--

CREATE TABLE bn_nodes_spacemodules (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    node integer NOT NULL PRIMARY KEY REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address_space integer REFERENCES bn_address_spaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_nodes_spacemodules IS 'This table holds the information about a nodes module association when in a project.';
COMMENT ON COLUMN bn_nodes_spacemodules.module_id IS 'The module id the node is associated to.';
COMMENT ON COLUMN bn_nodes_spacemodules.node IS 'The id of the node.';
COMMENT ON COLUMN bn_nodes_spacemodules.address_space IS 'The id of the address space.';

CREATE INDEX bn_nodes_spacemodules_address_space_idx
  ON bn_nodes_spacemodules USING btree (address_space);

--
-- bn_operands
--

CREATE TABLE bn_operands (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address bigint NOT NULL,
    expression_tree_id integer NOT NULL,
    "position" integer NOT NULL,
	CONSTRAINT bn_operands_pkey PRIMARY KEY (module_id, address, "position")
);

COMMENT ON TABLE bn_operands IS 'This table stores the information about operands.';
COMMENT ON COLUMN bn_operands.module_id IS 'The module id the operand belongs to.';
COMMENT ON COLUMN bn_operands.address IS 'The address where the operand can be found.';
COMMENT ON COLUMN bn_operands.expression_tree_id IS 'The expression tree id of the operand.';
COMMENT ON COLUMN bn_operands."position" IS 'The position of the operand.';

CREATE INDEX bn_operands_module_id_idx
  ON bn_operands USING btree (module_id);

--
-- bn_project_debuggers
--

CREATE TABLE bn_project_debuggers (
    project_id integer NOT NULL REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    debugger_id integer NOT NULL REFERENCES bn_debuggers(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_project_debuggers IS 'This table stores the information about debuggers associated to projects.';
COMMENT ON COLUMN bn_project_debuggers.project_id IS 'The id of a project.';
COMMENT ON COLUMN bn_project_debuggers.debugger_id IS 'The id of a debugger.';

CREATE INDEX bn_project_debuggers_debugger_id_idx
  ON bn_project_debuggers USING btree (debugger_id);

--
-- bn_project_settings
--

CREATE TABLE bn_project_settings (
    project_id integer NOT NULL REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    name character varying(255) NOT NULL,
    value text NOT NULL,
	CONSTRAINT bn_project_settings_pkey PRIMARY KEY (project_id, name)
);

COMMENT ON TABLE bn_project_settings IS 'This table stores all settings of a project.';
COMMENT ON COLUMN bn_project_settings.project_id IS 'The id of the project the setting belongs to.';
COMMENT ON COLUMN bn_project_settings.name IS 'The name of the setting.';
COMMENT ON COLUMN bn_project_settings.value IS 'The value of the setting.';

--
-- bn_project_traces
--

CREATE TABLE bn_project_traces (
    project_id integer NOT NULL REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    trace_id integer NOT NULL REFERENCES bn_traces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_project_traces_pkey PRIMARY KEY (project_id, trace_id)
);

COMMENT ON TABLE bn_project_traces IS 'This table stores the association between a project and a trace.';
COMMENT ON COLUMN bn_project_traces.project_id IS 'The id of the project';
COMMENT ON COLUMN bn_project_traces.trace_id IS 'The id of the trace.';

CREATE INDEX bn_project_traces_trace_id_idx
  ON bn_project_traces USING btree (trace_id);

--
-- bn_project_views
--

CREATE TABLE bn_project_views (
    project_id integer NOT NULL REFERENCES bn_projects(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    view_id integer NOT NULL PRIMARY KEY REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_project_views IS 'This table stores the association of a view to a project.';
COMMENT ON COLUMN bn_project_views.project_id IS 'The id of the project.';
COMMENT ON COLUMN bn_project_views.view_id IS 'The id of the view.';

CREATE INDEX bn_project_views_project_id_idx
    ON bn_project_views USING btree (project_id);

CREATE TRIGGER bn_project_views_trigger
    AFTER INSERT OR UPDATE OR DELETE
    ON bn_project_views
    FOR EACH ROW
    EXECUTE PROCEDURE bn_project_views_trigger();

--
-- bn_space_modules
--

CREATE TABLE bn_space_modules (
    module_id integer NOT NULL REFERENCES bn_modules(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    address_space_id integer NOT NULL REFERENCES bn_address_spaces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    image_base bigint NOT NULL,
	CONSTRAINT bn_space_modules_pkey PRIMARY KEY (address_space_id, module_id)
);

COMMENT ON TABLE bn_space_modules IS 'This table stores the association between modules and address spaces.';
COMMENT ON COLUMN bn_space_modules.module_id IS 'The id of the module.';
COMMENT ON COLUMN bn_space_modules.address_space_id IS 'The id of the address space.';
COMMENT ON COLUMN bn_space_modules.image_base IS 'The image base of the module in the context of the address space.';

--
-- bn_tags
--

CREATE SEQUENCE bn_tags_id_seq;
COMMENT ON SEQUENCE bn_tags_id_seq
    IS 'This sequence is used by the table bn_tags id field.';

CREATE TABLE bn_tags (
    id integer NOT NULL PRIMARY KEY DEFAULT nextval('bn_tags_id_seq'::regclass),
    parent_id integer REFERENCES bn_tags(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    name text NOT NULL,
    description text NOT NULL,
    type tag_type NOT NULL
);

COMMENT ON TABLE bn_tags IS 'This table stores all information about tags';
COMMENT ON COLUMN bn_tags.id IS 'The id of the tag.';
COMMENT ON COLUMN bn_tags.parent_id IS 'The id of a potential parent tag.';
COMMENT ON COLUMN bn_tags.name IS 'The name of the tag.';
COMMENT ON COLUMN bn_tags.description IS 'The description of the tag.';
COMMENT ON COLUMN bn_tags.type IS 'The type of the tag.';

ALTER SEQUENCE bn_tags_id_seq OWNED BY bn_tags.id;

CREATE INDEX bn_tags_parent_id_idx
    ON bn_tags USING btree (parent_id);

--
-- bn_tagged_nodes
--

CREATE TABLE bn_tagged_nodes (
    node_id integer NOT NULL REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    tag_id integer NOT NULL REFERENCES bn_tags(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_tagged_nodes_pkey PRIMARY KEY (node_id, tag_id)
);

COMMENT ON TABLE bn_tagged_nodes IS 'This table stores the association between tags and nodes.';
COMMENT ON COLUMN bn_tagged_nodes.node_id IS 'The node id of a tagged node.';
COMMENT ON COLUMN bn_tagged_nodes.tag_id IS 'The id of a tag.';

--
-- bn_tagged_views
--

CREATE TABLE bn_tagged_views (
    view_id integer NOT NULL REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    tag_id integer NOT NULL REFERENCES bn_tags(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT bn_tagged_views_pkey PRIMARY KEY (view_id, tag_id)
);

COMMENT ON TABLE bn_tagged_views IS 'This table stores the association between tags and views.';
COMMENT ON COLUMN bn_tagged_views.view_id IS 'The view id of a tagged view.';
COMMENT ON COLUMN bn_tagged_views.tag_id IS 'The id of a tag.';

CREATE INDEX bn_tagged_views_tag_id_idx
    ON bn_tagged_views USING btree (tag_id);

--
-- bn_text_nodes
--

CREATE TABLE bn_text_nodes (
    node_id integer NOT NULL PRIMARY KEY REFERENCES bn_nodes(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    comment_id integer REFERENCES bn_comments(id) ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
);

COMMENT ON TABLE bn_text_nodes IS 'This table stores the information about text nodes.';
COMMENT ON COLUMN bn_text_nodes.node_id IS 'The node id of the text node.';
COMMENT ON COLUMN bn_text_nodes.comment_id IS 'The id of the comment associated to the text node.';

CREATE INDEX bn_text_nodes_comment_id_idx
  ON bn_text_nodes USING btree (comment_id);

CREATE TRIGGER bn_text_nodes_comment_trigger
  AFTER INSERT OR DELETE OR UPDATE ON bn_text_nodes
  FOR EACH ROW
    EXECUTE PROCEDURE bn_text_nodes_comment_trigger();

--
-- bn_trace_event_values
--

CREATE TABLE bn_trace_event_values (
    trace_id integer NOT NULL REFERENCES bn_traces(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    "position" integer NOT NULL,
    register_name character varying(50) NOT NULL,
    register_value bigint NOT NULL,
    memory_value bytea NOT NULL,
	CONSTRAINT bn_trace_event_values_pkey PRIMARY KEY (trace_id, "position", register_name)
);

COMMENT ON TABLE bn_trace_event_values IS 'This table holds the information about register contents for a trace event.';
COMMENT ON COLUMN bn_trace_event_values.trace_id IS 'The id of the trace the event values are associated to.';
COMMENT ON COLUMN bn_trace_event_values."position" IS 'The position of the values within the trace.';
COMMENT ON COLUMN bn_trace_event_values.register_name IS 'The name of the register for which we have the values.';
COMMENT ON COLUMN bn_trace_event_values.register_value IS 'The values of the register.';
COMMENT ON COLUMN bn_trace_event_values.memory_value IS 'The value of the memory pointed to by the register when there was valid memory available.';

CREATE INDEX bn_trace_event_values_trace_id_idx
  ON bn_trace_event_values USING btree (trace_id);

--
-- bn_view_settings
--

CREATE TABLE bn_view_settings (
    view_id integer NOT NULL REFERENCES bn_views(id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED,
    name character varying(255) NOT NULL,
    value text NOT NULL,
	CONSTRAINT bn_view_settings_pkey PRIMARY KEY (view_id, name)
);

COMMENT ON TABLE bn_view_settings IS 'This table holds configuration settings for a particular view.';
COMMENT ON COLUMN bn_view_settings.view_id IS 'The view id associated to this setting.';
COMMENT ON COLUMN bn_view_settings.name IS 'The name of the setting.';
COMMENT ON COLUMN bn_view_settings.value IS 'The value of the setting.';


--
-- Function section.
--

--
-- import(IN rawmoduleid integer, IN moduleid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION import(IN rawmoduleid integer, IN moduleid integer, IN userid integer)
  RETURNS void AS
$$
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
           parent_module_name, parent_module_id, parent_module_function, comment_id, stack_frame, prototype)
           SELECT '|| moduleid ||', address, demangled_name, name, (ENUM_RANGE(NULL::function_type))[type + 1],
           null, module_name, null, null, null, stack_frame, prototype
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
$$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION import(IN rawmoduleid integer, IN moduleid integer, IN userid integer)
  IS 'This function performs all necessary conversions to transform a raw module from the exporter tables into a BinNavi type module.';

--
-- load_code_nodes(IN viewid integer)
--

CREATE OR REPLACE FUNCTION load_code_nodes(
  IN viewid integer)
  RETURNS TABLE (
    module_id integer,
    instruction_address bigint,
    operand_position integer,
    view_id integer,
    node_id integer,
    parent_function bigint,
    local_code_node_comment integer,
    global_code_node_comment integer,
    local_instruction_comment integer,
    global_instruction_comment integer,
    instruction_data bytea,
    x  double precision,
    y  double precision,
    width  double precision,
    height  double precision,
    color integer,
    bordercolor integer,
    selected boolean,
    visible boolean,
    mnemonic text,
    architecture architecture_type,
    expression_tree_id integer,
    expression_tree_type integer,
    symbol text,
    immediate bigint,
    expression_tree_parent_id integer,
    replacement text,
    target bigint,
    expression_types_type integer,
    expression_types_offset integer,
    expression_types_position integer,
    expression_types_path integer[],
    address_references_type address_reference_type,
    function_address bigint,
    type_instance_id integer)
    LANGUAGE SQL AS
$$

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

$$;
COMMENT ON FUNCTION load_code_nodes(
  IN viewid integer)
  IS 'Loads the code nodes for a view.';

--
-- load_function_information(IN moduleid integer, IN address bigint)
--

CREATE OR REPLACE FUNCTION load_function_information(IN moduleid integer, IN address bigint)
  RETURNS TABLE(
    view_id integer,
    address bigint,
    name text,
    original_name text,
    description text,
    bbcount bigint,
    edgecount bigint,
    incount bigint,
    outcount bigint,
    global_comment integer,
    type function_type,
    parent_module_name text,
    parent_module_id integer,
    parent_module_function integer,
    stack_frame integer,
    prototype integer)
  LANGUAGE SQL AS
$$

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

$$;
COMMENT ON FUNCTION load_function_information(IN moduleid integer, IN address bigint)
  IS 'This function provides the information about a single view / function information stored in the database.';

--
-- load_function_information(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION load_function_information(IN moduleid integer)
  RETURNS TABLE(
    view_id integer,
    address bigint,
    name text,
    original_name text,
    description text,
    bbcount bigint,
    edgecount bigint,
    incount bigint,
    outcount bigint,
    global_comment integer,
    type function_type,
    parent_module_name text,
    parent_module_id integer,
    parent_module_function integer,
    stack_frame integer,
    prototype integer) AS
$$

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
 parent_module_id, parent_module_function, stack_frame, prototype FROM bn_functions AS ft
 JOIN bn_function_views AS fviews ON fviews.module_id = ft.module_id
  AND function = ft.address
 JOIN function_block_count AS bc ON bc.function = ft.address
 JOIN function_edge_count AS ec ON ec.function = ft.address
 JOIN function_in_count AS ic ON ic.function = ft.address
 JOIN function_out_count AS oc ON oc.function = ft.address
 WHERE ft.module_id = $1
 ORDER BY ft.address

$$
  LANGUAGE SQL;
COMMENT ON FUNCTION load_function_information(IN moduleid integer)
  IS 'This function provides the information about all view / function information stored in the database under a specific module id.';

--
-- load_project_flowgraph(IN projectid integer, IN viewid integer)
--

CREATE OR REPLACE FUNCTION load_project_flowgraph(IN projectid integer, IN viewid integer)
  RETURNS TABLE(
    view_id integer,
    name text,
    description text,
    type view_type,
    creation_date timestamp without time zone,
    modification_date timestamp without time zone,
    stared boolean,
    bbcount bigint,
    edgecount bigint,
    type_count bigint,
    node_type node_type) AS
$$

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
$$
  LANGUAGE SQL;
COMMENT ON FUNCTION load_project_flowgraph(IN projectid integer, IN viewid integer)
  IS 'This function loads project view specific configuration information from the database.';

--
-- load_module_call_graph(IN moduleid integer, IN viewtype view_type)
--

CREATE OR REPLACE FUNCTION load_module_call_graph(IN moduleid integer, IN viewtype view_type)
  RETURNS TABLE(
    view_id integer,
    name text,
    description text,
    type view_type,
    creation_date timestamp without time zone,
    modification_date timestamp without time zone,
    stared boolean,
    bbcount bigint,
    edgecount bigint,
    type_count bigint,
    node_type node_type) AS
$$
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
$$
  LANGUAGE SQL;
COMMENT ON FUNCTION load_module_call_graph(IN moduleid integer, IN viewtype view_type)
  IS 'This function loads module call graph information. But not the call graph itself.';

--
-- load_project_call_graph(IN projectid int)
--

CREATE OR REPLACE FUNCTION load_project_call_graph(IN projectid int)
RETURNS TABLE(
    view_id int,
    name text,
    description text,
    type view_type,
    creation_date timestamp,
    modification_date timestamp,
    stared boolean,
    bbcount bigint,
    edgecount bigint,
    type_count bigint,
    node_type node_type) AS
$$
SELECT vt.id AS view_id, name, description,  vt.type AS type, creation_date, modification_date, stared,
  count(DISTINCT(nt.id)) AS bbcount,
  count(et.id) AS edgecount,
  count(DISTINCT(nt.type)) AS type_count, nt.type AS node_type
FROM bn_views AS vt
JOIN bn_project_views AS pvt ON vt.id = pvt.view_id
LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
  WHERE pvt.project_id = $1
GROUP BY vt.id, nt.type
HAVING COUNT(DISTINCT(nt.type)) = 1
  AND nt.type = 'function'
ORDER BY vt.id;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION load_project_call_graph(IN projectid int)
  IS 'This function loads project call graph information. But not the call graph itself.';

--
-- get_derived_views(IN viewid integer)
--

CREATE OR REPLACE FUNCTION get_derived_views(IN viewid integer)
RETURNS integer AS $$

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

$$ LANGUAGE SQL;
COMMENT ON FUNCTION get_derived_views(IN viewid integer)
  IS 'This function retrieves the views from the database which have been derived from the given view.';

--
-- create_native_call_graph_view(IN moduleid int)
--

CREATE OR REPLACE FUNCTION create_native_call_graph_view(IN moduleid integer)
RETURNS int AS $$

WITH callgraphid AS (
  INSERT INTO bn_views
    (type, name, description, creation_date, modification_date)
    VALUES('native', 'Native Callgraph', null, NOW(), NOW())
    RETURNING id
)
INSERT INTO bn_module_views (view_id, module_id)
  SELECT id, $1 FROM callgraphid
  RETURNING view_id;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION create_native_call_graph_view(IN moduleid integer)
  IS 'This function creates the native call graph information and updates the tables accordingly.';

--
-- create_native_flowgraph_views(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION create_native_flowgraph_views(IN moduleid integer)
  RETURNS void AS
$$

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

$$ LANGUAGE SQL;
COMMENT ON FUNCTION create_native_flowgraph_views(IN moduleid integer)
  IS 'This function creates the native flowgraph information in the database during a conversion and updates the related tables accordingly.';

--
-- create_native_flowgraph_edges(IN rawmoduleid integer, IN moduleid integer)
--

CREATE OR REPLACE FUNCTION create_native_flowgraph_edges(IN rawmoduleid integer, IN moduleid integer)
  RETURNS void AS
$$
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
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION create_native_flowgraph_edges(IN rawmoduleid integer, IN moduleid integer)
  IS 'This function creates the edges in the native flowgraphs during a rawmodule to module converstion.';

--
-- colorize_module_nodes(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION colorize_module_nodes(IN moduleid integer) RETURNS void
  LANGUAGE sql
  AS $$
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
$$;
COMMENT ON FUNCTION colorize_module_nodes(IN moduleid integer)
  IS 'This function creates the initial colors for nodes of a module that is getting converted.';

--
-- load_module_flowgraph_information(IN moduleid integer, IN viewid integer)
--

CREATE OR REPLACE FUNCTION load_module_flowgraph_information(IN moduleid integer, IN viewid integer)
  RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone, modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint, node_type node_type) AS
$$

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

$$ LANGUAGE SQL;
COMMENT ON FUNCTION load_module_flowgraph_information(IN moduleid integer, IN viewid integer)
  IS 'This function loads aggregated information about a specific view / function from the database.';

--
-- load_module_node_tags(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION load_module_node_tags(IN moduleid integer)
  RETURNS TABLE (view_id int, tag_id int) AS $$

  SELECT nt.view_id, tnt.tag_id FROM bn_tagged_nodes AS tnt
        JOIN bn_nodes AS nt ON node_id = nt.id
        JOIN bn_module_views AS mvt ON mvt.view_id = nt.view_id
      WHERE mvt.module_id = $1
      GROUP BY nt.view_id, tnt.tag_id
      ORDER BY nt.view_id;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION load_module_node_tags(IN moduleid integer)
  IS 'Loads the node tags for the module given as argument.';

--
-- load_project_node_tags(IN projectid integer)
--

CREATE OR REPLACE FUNCTION load_project_node_tags(IN projectid integer)
  RETURNS TABLE (view_id int, tag_id int) AS $$

  SELECT nt.view_id, tnt.tag_id FROM bn_tagged_nodes AS tnt
        JOIN bn_nodes AS nt ON node_id = nt.id
        JOIN bn_project_views AS pvt ON pvt.view_id = nt.view_id
      WHERE pvt.project_id = $1
      GROUP BY nt.view_id, tnt.tag_id
      ORDER BY nt.view_id;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION load_project_node_tags(IN moduleid integer)
  IS 'Loads the node tags for the project given as argument.';

--
-- load_module_mixed_graph(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION load_module_mixed_graph(IN moduleid integer)
  RETURNS TABLE(view_id int, name text, description text, type view_type, creation_date timestamp,
  modification_date timestamp, stared boolean, bbcount bigint, edgecount bigint, type_count bigint) AS $$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count
    FROM bn_views AS vt
    JOIN bn_module_views AS mvt ON vt.id = mvt.view_id
    LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
    LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
      WHERE vt.type = 'non-native' AND mvt.module_id = $1
      GROUP BY vt.id
      HAVING COUNT(DISTINCT(nt.type)) = 2

$$ LANGUAGE SQL;
COMMENT ON FUNCTION load_module_mixed_graph(IN moduleid integer)
  IS 'This function loads all mixed graph information for the specified module from the database.';

--
-- load_project_mixed_graph(IN projectid integer)
--

CREATE OR REPLACE FUNCTION load_project_mixed_graph(IN projectid integer)
  RETURNS TABLE(view_id int, name text, description text, type view_type, creation_date timestamp,
  modification_date timestamp, stared boolean, bbcount bigint, edgecount bigint, type_count bigint) AS $$

  SELECT vt.id AS view_id, name, description, vt.type AS type, creation_date, modification_date, stared,
    COUNT(DISTINCT(nt.id)) AS bbcount, COUNT(et.id) AS edgecount, COUNT(DISTINCT(nt.type)) AS type_count
    FROM bn_views AS vt
    JOIN bn_project_views AS mvt ON vt.id = pvt.view_id
    LEFT JOIN bn_nodes AS nt ON vt.id = nt.view_id
    LEFT JOIN bn_edges AS et ON nt.id = et.source_node_id
      WHERE vt.type = 'non-native' AND pvt.project_id = $1
      GROUP BY vt.id
      HAVING COUNT(DISTINCT(nt.type)) in (0,2)

$$ LANGUAGE SQL;
COMMENT ON FUNCTION load_project_mixed_graph(IN projectid integer)
  IS 'This function loads all mixed graph information for the specified project from the database.';

--
-- load_module_flow_graphs(IN moduleid integer, IN viewtype view_type)
--

CREATE OR REPLACE FUNCTION load_module_flow_graphs(IN moduleid integer, IN viewtype view_type)
  RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone,
  modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint,
  node_type node_type) AS
$$

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

$$ LANGUAGE SQL;
COMMENT ON FUNCTION load_module_flow_graphs(IN moduleid integer, IN viewtype view_type)
  IS 'This function loads flow graph type graphs for a module.';

--
-- load_module_call_graphs(IN moduleid integer, IN viewtype view_type)
--

CREATE OR REPLACE FUNCTION load_module_call_graphs(IN moduleid integer, IN viewtype view_type)
  RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone,
  modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint,
  node_type node_type) AS
$$

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

$$ LANGUAGE sql;
COMMENT ON FUNCTION load_module_call_graphs(IN moduleid integer, IN viewtype view_type)
  IS 'This function loads call graph type graphs for a module.';

--
-- load_project_flow_graphs(IN moduleid integer, IN viewtype view_type)
--

CREATE OR REPLACE FUNCTION load_project_flow_graphs(IN projectid integer, IN viewtype view_type)
  RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone,
  modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint,
  node_type node_type) AS
$$

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

$$ LANGUAGE SQL;
COMMENT ON FUNCTION load_project_flow_graphs(IN projectid integer, IN viewtype view_type)
  IS 'This function loads flow graph type graphs for a project.';

--
-- load_project_call_graphs(IN moduleid integer, IN viewtype view_type)
--

CREATE OR REPLACE FUNCTION load_project_call_graphs(IN projectid integer, IN viewtype view_type)
  RETURNS TABLE(view_id integer, name text, description text, type view_type, creation_date timestamp without time zone,
  modification_date timestamp without time zone, stared boolean, bbcount bigint, edgecount bigint, type_count bigint,
  node_type node_type) AS
$$

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

$$ LANGUAGE sql;
COMMENT ON FUNCTION load_project_call_graphs(IN projectid integer, IN viewtype view_type)
  IS 'This function loads call graph type graphs for a project.';


--
-- connect_instructions_to_code_nodes(IN rawmoduleid integer, IN moduleid integer)
--

CREATE OR REPLACE FUNCTION connect_instructions_to_code_nodes(IN rawmoduleid integer, IN moduleid integer)
  RETURNS void AS
$$
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
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION connect_instructions_to_code_nodes(IN rawmoduleid integer, IN moduleid integer)
  IS 'This function connects the instruction to code nodes in the conversion from a raw module to a BinNavi module.';

--
-- create_native_callgraph_edges(IN rawmoduleid integer, IN moduleid integer)
--

CREATE OR REPLACE FUNCTION create_native_callgraph_edges(IN rawmoduleid integer, IN moduleid integer)
  RETURNS void AS
$$
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
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION create_native_callgraph_edges(IN rawmoduleid integer, IN moduleid integer)
  IS 'This function creates the edges for the native call graph of a module.';

--
-- create_native_code_nodes(IN rawmoduleid integer, IN moduleid integer)
--

CREATE OR REPLACE FUNCTION create_native_code_nodes(IN rawmoduleid integer, IN moduleid integer)
 RETURNS void AS
$$
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
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION create_native_code_nodes(IN rawmoduleid integer, IN moduleid integer)
  IS 'This function creates the code nodes for all native flow graphs in a raw module to BinNavi module conversion.';

--
-- create_native_callgraph_nodes(IN viewid integer, IN moduleid integer)
--

CREATE OR REPLACE FUNCTION create_native_callgraph_nodes(IN viewid integer, IN moduleid integer)
  RETURNS void AS
$$
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
$$ LANGUAGE SQL;
COMMENT ON FUNCTION create_native_callgraph_nodes(IN viewid integer, IN moduleid integer)
  IS 'This function creates the function nodes for the native call graph in a raw module to BinNavi module conversion.';

--
-- create_module(IN rawmoduleid integer)
--

CREATE OR REPLACE FUNCTION create_module(IN rawmoduleid integer)
  RETURNS int AS
$$
INSERT INTO bn_modules
  (name, raw_module_id, md5, sha1, description, file_base, image_base, import_time)
  (SELECT name, id, md5, sha1, comment, base_address, base_address, NOW()
  FROM modules WHERE id = $1) RETURNING id
$$ LANGUAGE SQL;
COMMENT ON FUNCTION create_module(IN rawmoduleid integer)
  IS 'This function creates the entry in the BinNavi modules table in a raw module to BinNavi module conversion.';

--
-- set_section_name(IN moduleid integer, IN sectionid integer, IN newname text)
--

CREATE OR REPLACE FUNCTION set_section_name(IN moduleid integer, IN sectionid integer, IN newname text)
    RETURNS void AS $$

    UPDATE bn_sections
       SET name = $3
     WHERE module_id = $1
       AND id = $2

$$ LANGUAGE SQL;
COMMENT ON FUNCTION set_section_name(IN moduleid integer, IN sectionid integer, IN newname text)
  IS 'This function creates a new entry in the section table of BinNavi and returns the id of the entry.';

--
-- get_sections(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION get_sections(IN moduleid integer)
    RETURNS TABLE (module_id integer, id integer, name text, comment_id integer, start_address bigint,
                   end_address bigint, permission permission_type, data bytea) AS $$

    SELECT *
      FROM bn_sections
     WHERE module_id = $1;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION get_sections(IN moduleid integer)
  IS 'This function returns all sections associated with the given module id.';

--
-- create_section(IN moduleid integer, name text, comment_id integer, start_address bigint, end_address bigint, permission permission_type, data bytea)
--

CREATE OR REPLACE FUNCTION create_section(IN moduleid integer, name text, comment_id integer, start_address bigint, end_address bigint, permission permission_type, data bytea)
    RETURNS integer AS $$

    INSERT INTO bn_sections (module_id, name, comment_id, start_address, end_address, permission, data)
         VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION create_section(IN moduleid integer, name text, comment_id integer, start_address bigint,
                   end_address bigint, permission permission_type, data bytea)
  IS 'This function creates a new section and returns its id';

--
-- delete_section(IN moduleid integer, IN sectionid integer)
--

CREATE OR REPLACE FUNCTION delete_section(IN moduleid integer, IN sectionid integer)
	RETURNS void AS $$

	DELETE
	 FROM bn_sections
	 WHERE module_id = $1
	 AND id = $2;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION delete_section(IN moduleid integer, IN sectionid integer)
  IS 'This function deletes a section.';

--
-- delete_expression_type_instance(IN moduleid integer, IN address bigint, IN "position" integer, IN expressionid integer)
--

CREATE OR REPLACE FUNCTION delete_expression_type_instance(IN moduleid integer, IN address bigint, IN "position" integer, IN expressionid integer)
    RETURNS void AS $$

    DELETE
     FROM bn_expression_type_instances
     WHERE module_id = $1
       AND address = $2
       AND "position" = $3
       AND expression_id = $4;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION delete_expression_type_instance(IN moduleid integer, IN address bigint, IN "position" integer, IN expressionid integer)
  IS 'This function deletes a expression type instance (cross reference).';

--
-- delete_type_instance(IN moduleid integer, IN typeinstanceid integer)
--

CREATE OR REPLACE FUNCTION delete_type_instance(IN moduleid integer, IN typeinstanceid integer)
	RETURNS void AS $$

	DELETE
     FROM bn_type_instances
     WHERE module_id = $1
     AND id = $2;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION delete_type_instance(IN moduleid integer, IN typeinstanceid integer)
  IS 'This function delete a type instance.';

--
-- load_expression_type_instances(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION load_expression_type_instances(
    IN moduleid integer)
	RETURNS TABLE(
        view_id integer,
        module_id integer,
        address bigint,
        "position" integer,
        expression_id integer,
        type_instance_id integer)
        LANGUAGE SQL AS
$$

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

$$;
COMMENT ON FUNCTION load_expression_type_instances(
    IN moduleid integer)
    IS 'The function retrieves all expression type instances for a given module.';

--
-- load_expression_type_instance(IN moduleid integer, IN typeinstanceid integer, IN address bigint, IN "position" integer, IN expressionid integer)
--

CREATE OR REPLACE FUNCTION load_expression_type_instance(
    IN moduleid integer,
	IN typeinstanceid integer,
	IN address bigint,
	IN "position" integer,
	IN expressionid integer)
	RETURNS TABLE (
	    view_id integer,
		module_id integer,
		address bigint,
		"position" integer,
		expression_id integer,
		type_instance_id integer)
	LANGUAGE SQL AS
$$

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

$$;

COMMENT ON FUNCTION load_expression_type_instance(
    IN moduleid integer,
    IN typeinstanceid integer,
    IN address bigint,
    IN "position" integer,
    IN expressionid integer)
    IS 'Loads a single cross reference from the database.';

--
-- create_type_instance(moduleid integer, instancename text, commentid integer, typeid integer, sectionid integer, instanceaddress bigint)
--

CREATE OR REPLACE FUNCTION create_type_instance(
  moduleid integer,
  instancename text,
  commentid integer,
  typeid integer,
  sectionid integer,
  instanceaddress bigint)
  RETURNS integer
  LANGUAGE SQL AS
$$

    INSERT INTO bn_type_instances (module_id, name, comment_id, type_id, section_id, section_offset)
        VALUES ($1, $2, $3, $4, $5, $6) RETURNING id;

$$;
COMMENT ON FUNCTION create_type_instance(integer, text, integer, integer, integer, bigint)
  IS 'This function creates a new type instance and returns the generated id of it.';

--
-- load_type_instances(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION load_type_instances(
    IN moduleid integer)
    RETURNS TABLE (
	  module_id integer,
	  id integer,
	  name text,
	  comment_id integer,
	  type_id integer,
	  section_id integer,
	  section_offset bigint)
	  LANGUAGE SQL AS
$$
    SELECT * FROM bn_type_instances
     WHERE module_id = $1;

$$;
COMMENT ON FUNCTION load_type_instances(
  IN moduleid integer)
  IS 'This function retrives all type instaces for a given module id.';

--
-- load_type_instance(IN moduleid integer, IN typeinstanceid integer)
--

CREATE OR REPLACE FUNCTION load_type_instance(
    IN moduleid integer,
    IN typeinstanceid integer)
    RETURNS TABLE (
	  module_id integer,
	  id integer,
	  name text,
	  comment_id integer,
	  type_id integer,
	  section_id integer,
	  section_offset bigint)
	  LANGUAGE SQL AS
$$
    SELECT * FROM bn_type_instances
     WHERE module_id = $1
	   AND id = $2;
$$;
COMMENT ON FUNCTION load_type_instance(
  IN moduleid integer,
  IN typeinstanceid integer)
  IS 'This function loads a single type instaces.';

--
-- create_expression_type_instance(moduleid integer, operandaddress bigint, operandposition integer, expressionid integer, typeinstanceid integer)
--

CREATE OR REPLACE FUNCTION create_expression_type_instance(
  moduleid integer,
  operandaddress bigint,
  operandposition integer,
  expressionid integer,
  typeinstanceid integer)
  RETURNS void AS
$$

    INSERT INTO bn_expression_type_instances (module_id, address, position, expression_id, type_instance_id)
        VALUES ($1, $2, $3, $4, $5);

$$ LANGUAGE SQL;
COMMENT ON FUNCTION create_expression_type_instance(integer, bigint, integer, integer, integer)
  IS 'This function creates a new expression type instance which connects a type instance in a section to an operand in the graph view.';

--
-- set_type_instance_name(moduleid integer, typeinstanceid integer, newname text)
--

CREATE OR REPLACE FUNCTION set_type_instance_name(moduleid integer, typeinstanceid integer, newname text)
  RETURNS void AS
$$

    UPDATE bn_type_instances
       SET name = $3
     WHERE module_id = $1
       AND id = $2;

$$ LANGUAGE SQL;
COMMENT ON FUNCTION set_type_instance_name(integer, integer, text)
  IS 'This functions sets the name of a type instance.';


--
-- append_comment(IN parent_id integer, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_comment(IN parent_id integer, IN user_id integer, IN comment_text text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
 $$;
COMMENT ON FUNCTION append_comment(IN parent_id integer, IN user_id integer, IN comment_text text)
  IS 'This function appends a comment to a comment owned by the user given as argument.
  It returns the generated id of the comment.
  This function is used by all other append comment functions.';

--
-- append_group_node_comment(IN nodeId integer, IN userId integer, IN comment text)
--

CREATE OR REPLACE FUNCTION append_group_node_comment(IN nodeId integer, IN userId integer, IN comment text) RETURNS integer
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
COMMENT ON FUNCTION append_group_node_comment(IN nodeId integer, IN userId integer, IN comment text)
  IS 'This function appends a comment to a group node. It returns the generated id of the comment.';

--
-- append_text_node_comment(IN nodeId integer, IN userId integer, IN comment text)
--

CREATE OR REPLACE FUNCTION append_text_node_comment(IN nodeId integer, IN userId integer, IN comment text) RETURNS integer
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
COMMENT ON FUNCTION append_text_node_comment(IN nodeId integer, IN userId integer, IN comment text)
  IS 'This function appends a comment to a text node. It returns the generated id of the comment.';

--
-- append_function_comment(IN moduleid integer, IN functionaddress bigint, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_function_comment(IN moduleid integer, IN functionaddress bigint, IN user_id integer, IN comment_text text) RETURNS integer
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
COMMENT ON FUNCTION append_function_comment(IN moduleid integer, IN functionaddress bigint, IN user_id integer, IN comment_text text)
  IS 'This function appends a comment to a function. It returns the generated id of the comment.';

--
-- append_function_node_comment(IN moduleid integer, IN nodeid integer, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_function_node_comment(IN moduleid integer, IN nodeid integer, IN user_id integer, IN comment_text text) RETURNS integer
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
COMMENT ON FUNCTION append_function_node_comment(IN moduleid integer, IN nodeid integer, IN user_id integer, IN comment_text text)
  IS 'This function appends a comment to a function node. It returns the generated id of the comment.';

--
-- append_global_code_node_comment(IN moduleid integer, IN node_id integer, IN node_address bigint, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_global_code_node_comment(IN moduleid integer, IN node_id integer, IN node_address bigint, IN user_id integer, IN comment_text text) RETURNS integer
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
COMMENT ON FUNCTION append_global_code_node_comment(IN moduleid integer, IN node_id integer, IN node_address bigint, IN user_id integer, IN comment_text text)
  IS 'This function appends a global code node comment. It returns the generated id of the comment.';

--
-- append_global_edge_comment(IN srcmoduleid integer, IN dstmoduleid integer, IN srcnodeaddress bigint, IN dstnodeaddress bigint, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_global_edge_comment(IN srcmoduleid integer, IN dstmoduleid integer, IN srcnodeaddress bigint, IN dstnodeaddress bigint, IN user_id integer, IN comment_text text) RETURNS integer
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
COMMENT ON FUNCTION append_global_edge_comment(IN srcmoduleid integer, IN dstmoduleid integer, IN srcnodeaddress bigint, IN dstnodeaddress bigint, IN user_id integer, IN comment_text text)
  IS 'This function appends a global edge comment. It returns the generated id of the comment.';

--
-- append_global_instruction_comment(IN moduleid integer, IN instruction_address bigint, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_global_instruction_comment(IN moduleid integer, IN instruction_address bigint, IN user_id integer, IN comment_text text) RETURNS integer
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
COMMENT ON FUNCTION append_global_instruction_comment(IN moduleid integer, IN instruction_address bigint, IN user_id integer, IN comment_text text)
  IS 'This function appends a global instruction comment. It returns the generated id of the comment.';

--
-- append_local_code_node_comment(IN moduleid integer, IN nodeid integer, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_local_code_node_comment(IN moduleid integer, IN nodeid integer, IN user_id integer, IN comment_text text) RETURNS integer
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
COMMENT ON FUNCTION append_local_code_node_comment(IN moduleid integer, IN nodeid integer, IN user_id integer, IN comment_text text)
  IS 'This function appends a local comment to a code node. It returns the generated id of the comment.';

--
-- append_local_edge_comment(IN edge_id integer, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_local_edge_comment(IN edge_id integer, IN user_id integer, IN comment_text text) RETURNS integer
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
COMMENT ON FUNCTION append_local_edge_comment(IN edge_id integer, IN user_id integer, IN comment_text text)
  IS 'This function appends a local edge comment to an edge. It returns the generated id of the comment.';

--
-- append_local_instruction_comment(IN moduleid integer, IN nodeid integer, IN instruction_address bigint, IN user_id integer, IN comment_text text)
--

CREATE OR REPLACE FUNCTION append_local_instruction_comment(IN moduleid integer, IN nodeid integer, IN instruction_address bigint, IN user_id integer, IN comment_text text) RETURNS integer
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
COMMENT ON FUNCTION append_local_instruction_comment(IN moduleid integer, IN nodeid integer, IN instruction_address bigint, IN user_id integer, IN comment_text text)
  IS 'This function appends a local instruction comment to an instruction in a code node. It returns the generated id of the comment.';

--
-- append_section_comment(moduleid integer, sectionid integer, user_id integer, comment_text text)
--

CREATE OR REPLACE FUNCTION append_section_comment(moduleid integer, sectionid integer, user_id integer, comment_text text)
  RETURNS integer AS
$$
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
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION append_section_comment(integer, integer, integer, text)
   IS 'This function appends a comment to a section. It returns the generated id of the comment.';

--
-- append_type_instance_comment(moduleid integer, typeinstanceid integer, user_id integer, comment_text text)
--

CREATE OR REPLACE FUNCTION append_type_instance_comment(moduleid integer, typeinstanceid integer, user_id integer, comment_text text)
  RETURNS integer AS
$$
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
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION append_type_instance_comment(integer, integer, integer, text)
   IS 'This function appends a comment to a type instance. It returns the generated id of the comment.';

--
-- delete_group_node_comment(IN nodeId integer, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_group_node_comment(IN nodeId integer, IN commentid integer, IN userid integer)
  RETURNS integer
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
COMMENT ON FUNCTION delete_group_node_comment(IN nodeId integer, IN commentid integer, IN userid integer)
  IS 'This function deletes a comment from a group node. It returns the id of the deleted comment.';

--
-- delete_text_node_comment(IN nodeId integer, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_text_node_comment(IN nodeId integer, IN commentid integer, IN userid integer)
  RETURNS integer
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
COMMENT ON FUNCTION delete_text_node_comment(IN nodeId integer, IN commentid integer, IN userid integer)
  IS 'This function deletes a comment from a text node. It returns the id of the deleted comment.';

--
-- delete_comment_by_id(IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_comment_by_id(IN commentid integer, IN userid integer)
  RETURNS integer
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
COMMENT ON FUNCTION delete_comment_by_id(IN commentid integer, IN userid integer)
 IS 'This function deletes a comment by id. It is used by all other comment delete functions as core functionality.
 It does verify if the user id given as argument is the actual owner of the comment which is requested to be deleted and will fail otherwise.';

--
-- delete_function_comment(IN moduleid integer, IN functionaddress bigint, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_function_comment(IN moduleid integer, IN functionaddress bigint, IN commentid integer, IN userid integer)
  RETURNS integer
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
COMMENT ON FUNCTION delete_function_comment(IN moduleid integer, IN functionaddress bigint, IN commentid integer, IN userid integer)
  IS 'This function deletes a comment associated to a function. It does return the id of the deleted comment.';

--
-- delete_function_node_comment(IN moduleid integer, IN nodeid integer, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_function_node_comment(IN moduleid integer, IN nodeid integer, IN commentid integer, IN userid integer) RETURNS integer
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
COMMENT ON FUNCTION delete_function_node_comment(IN moduleid integer, IN nodeid integer, IN commentid integer, IN userid integer)
  IS 'This function deletes a comment associated to a function node. It returns the id of the deleted comment.';

--
-- delete_global_code_node_comment(IN moduleid integer, IN node_id integer, IN node_address bigint, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_global_code_node_comment(IN moduleid integer, IN node_id integer, IN node_address bigint, IN commentid integer, IN userid integer) RETURNS integer
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
COMMENT ON FUNCTION delete_global_code_node_comment(IN moduleid integer, IN node_id integer, IN node_address bigint, IN commentid integer, IN userid integer)
  IS 'This function deletes a global comment assocatiated with a code node. It returns the id of the deleted comment.';

--
-- delete_global_edge_comment(IN srcmoduleid integer, IN dstmoduleid integer, IN srcnodeaddress bigint, IN dstnodeaddress bigint, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_global_edge_comment(IN srcmoduleid integer, IN dstmoduleid integer, IN srcnodeaddress bigint, IN dstnodeaddress bigint, IN commentid integer, IN userid integer) RETURNS integer
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
COMMENT ON FUNCTION delete_global_edge_comment(IN srcmoduleid integer, IN dstmoduleid integer, IN srcnodeaddress bigint, IN dstnodeaddress bigint, IN commentid integer, IN userid integer)
  IS 'This function deletes a global comment associated with an edge. It does return the id of the deleted comment.';

--
-- delete_global_instruction_comment(IN moduleid integer, IN instruction_address bigint, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_global_instruction_comment(IN moduleid integer, IN instruction_address bigint, IN commentid integer, IN userid integer) RETURNS integer
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
COMMENT ON FUNCTION delete_global_instruction_comment(IN moduleid integer, IN instruction_address bigint, IN commentid integer, IN userid integer)
  IS 'This function deletes a global comment associated with an instruction. It returns the id of the deleted comment.';

--
-- delete_local_code_node_comment(IN moduleid integer, IN nodeid integer, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_local_code_node_comment(IN moduleid integer, IN nodeid integer, IN commentid integer, IN userid integer) RETURNS integer
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
COMMENT ON FUNCTION delete_local_code_node_comment(IN moduleid integer, IN nodeid integer, IN commentid integer, IN userid integer)
  IS 'This funciton deletes a local comment associated with a code node. It returns the id of the deleted comment.';

--
-- delete_local_edge_comment(IN edge_id integer, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_local_edge_comment(IN edge_id integer, IN commentid integer, IN userid integer) RETURNS integer
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
COMMENT ON FUNCTION delete_local_edge_comment(IN edge_id integer, IN commentid integer, IN userid integer)
  IS 'This function deletes a local comment associated with an edge. It returns the id of the deleted comment.';

--
-- delete_local_instruction_comment(IN moduleid integer, IN nodeid integer, IN instruction_address bigint, IN commentid integer, IN userid integer)
--

CREATE OR REPLACE FUNCTION delete_local_instruction_comment(IN moduleid integer, IN nodeid integer, IN instruction_address bigint, IN commentid integer, IN userid integer) RETURNS integer
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
COMMENT ON FUNCTION delete_local_instruction_comment(IN moduleid integer, IN nodeid integer, IN instruction_address bigint, IN commentid integer, IN userid integer)
  IS 'This function deletes a local comment associated with an instruction in a code node. It returns the id of the deleted comment.';

--
-- delete_section_comment(moduleid integer, sectionid integer, commentid integer, userid integer)
--

CREATE OR REPLACE FUNCTION delete_section_comment(moduleid integer, sectionid integer, commentid integer, userid integer)
  RETURNS integer AS
$$
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
$$
  LANGUAGE plpgsql;
COMMENT ON FUNCTION delete_section_comment(integer, integer, integer, integer)
   IS 'This function deletes a comment associated to a section. It does return the id of the deleted comment.';

--
-- delete_type_instance_comment(moduleid integer, typeinstanceid integer, commentid integer, userid integer)
--

CREATE OR REPLACE FUNCTION delete_type_instance_comment(moduleid integer, typeinstanceid integer, commentid integer, userid integer)
  RETURNS integer AS
$$
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
$$
  LANGUAGE plpgsql;
  COMMENT ON FUNCTION delete_type_instance_comment(integer, integer, integer, integer)
   IS 'This function deletes a comment associated to a type instance. It does return the id of the deleted comment.';

--
-- edit_comment(IN comment_id integer, IN userid integer, IN commenttext text)
--

CREATE OR REPLACE FUNCTION edit_comment(IN comment_id integer, IN userid integer, IN commenttext text) RETURNS void
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
COMMENT ON FUNCTION edit_comment(IN comment_id integer, IN userid integer, IN commenttext text)
  IS 'This function edits a comment based on the id and the comment text provided.
  It checks if the user given is the owner of the comment and will fail if this is not the case.
  This function is used for all edit functions.';

--
-- get_all_comment_ancestors(IN comment_id integer)
--

CREATE OR REPLACE FUNCTION get_all_comment_ancestors(IN comment_id integer)
  RETURNS TABLE(level integer, id integer, parent_id integer, user_id integer, comment text)
  LANGUAGE sql
  AS $$
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
 $$;
COMMENT ON FUNCTION get_all_comment_ancestors(IN comment_id integer)
  IS 'This function gets all ancestors of a comment for the provided comment id.';

--
-- get_all_comment_ancestors_multiple(IN commentids integer[])
--

CREATE OR REPLACE FUNCTION get_all_comment_ancestors_multiple(IN commentids integer[])
  RETURNS TABLE(commentid integer, level integer, id integer, parent_id integer, user_id integer, comment text)
  LANGUAGE plpgsql
  AS $$
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
$$;
COMMENT ON FUNCTION get_all_comment_ancestors_multiple(IN commentids integer[])
  IS 'This function gets all ancestors of all comment ids provided as argument.
  It can be used as to batch load comments and reduce the number of querries needed to do so.';

--
-- load_view_edges(IN viewid integer)
--

CREATE OR REPLACE FUNCTION load_view_edges(IN viewid integer)
  RETURNS TABLE(id integer, source_node_id integer, target_node_id integer, comment_id integer, x1 double precision, y1 double precision, x2 double precision, y2 double precision, type edge_type, color integer, visible boolean, selected boolean, x double precision, y double precision)
  LANGUAGE sql AS
$$
  SELECT edges.id, source_node_id, target_node_id,
  comment_id, x1, y1, x2, y2,
  edges.type, edges.color, edges.visible, edges.selected, ep.x, ep.y
  FROM bn_edges AS edges
    JOIN bn_nodes AS bt ON edges.target_node_id = bt.id
    JOIN bn_nodes AS bs ON edges.source_node_id = bs.id
    LEFT JOIN bn_edge_paths AS ep ON ep.edge_id = edges.id
  WHERE bt.view_id = $1
    AND bs.view_id = $1;
$$;
COMMENT ON FUNCTION load_view_edges(IN viewid integer)
  IS 'This function loads the edges of a view.';

--
-- create_type_substitution(IN moduleid integer, IN address bigint, IN position integer, IN expressionid integer, IN basetypeid integer, IN path integer[], IN offset integer)
--

CREATE OR REPLACE FUNCTION create_type_substitution(
  IN moduleid integer,
  IN address bigint,
  IN "position" integer,
  IN expressionid integer,
  IN basetypeid integer,
  IN path integer[],
  IN "offset" integer)
  RETURNS void
  LANGUAGE sql AS
$$
INSERT INTO bn_expression_types (module_id, address, position, expression_id, base_type_id, path, offset)
  VALUES($1, $2, $3, $4, $5, $6, $7);
$$;
COMMENT ON FUNCTION create_type_substitution(
  IN moduleid integer,
  IN address bigint,
  IN "position" integer,
  IN expressionid integer,
  IN basetypeid integer,
  IN path integer[],
  IN "offset" integer)
  IS 'This function creates a single type substitution';

--
-- delete_type_substitution(IN moduleid integer, IN address bigint, IN position integer, IN expressionid)
--

CREATE OR REPLACE FUNCTION delete_type_substitution(
  IN moduleid integer,
  IN address bigint,
  IN "position" integer,
  IN expressionid integer)
  RETURNS void
  LANGUAGE sql AS
$$
DELETE FROM bn_expression_types AS et
  WHERE et.module_id = $1
    AND et.address = $2
	AND et.position = $3
	AND et.expression_id = $4;
$$;
COMMENT ON FUNCTION delete_type_substitution(
  IN moduleid integer,
  IN address bigint,
  IN "position" integer,
  IN expressionid integer)
  IS 'This function deletes a single type substitution';

--
-- load_types(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION load_types(
  IN moduleid integer)
  RETURNS TABLE(
    id integer,
    name text,
    size integer,
    pointer integer,
    signed boolean,
    category type_category)
  LANGUAGE SQL AS
$$

SELECT id, name, size, pointer, signed, category
  FROM bn_base_types
 WHERE module_id = $1;

$$;
COMMENT ON FUNCTION load_types(
  IN moduleid integer)
  IS 'Loads all base types for a given module id.';

--
-- load_type(IN moduleid integer, IN typeid integer)
--

CREATE OR REPLACE FUNCTION load_type(
  IN moduleid integer,
  IN typeid integer)
  RETURNS TABLE(
    id integer,
    name text,
    size integer,
    pointer integer,
    signed boolean,
    category type_category)
  LANGUAGE SQL AS
$$

SELECT id, name, size, pointer, signed, category
  FROM bn_base_types
 WHERE module_id = $1
   AND id = $2;

$$;
COMMENT ON FUNCTION load_type(
  IN moduleid integer,
  IN typeid integer)
  IS 'Loads a single base type.';

--
-- load_type_member(IN moduleid integer, IN typeid integer)
--

CREATE OR REPLACE FUNCTION load_type_member(
  IN moduleid integer,
  IN typeid integer)
  RETURNS TABLE (
    id integer,
    name text,
	base_type integer,
	parent_id integer,
	"offset" integer,
	argument integer,
	number_of_elements integer)
  LANGUAGE SQL AS
$$

SELECT id, name, base_type, parent_id, "offset", argument, number_of_elements
  FROM bn_types
 WHERE module_id = $1
   AND id = $2;

$$;
COMMENT ON FUNCTION load_type_member(
  IN moduleid integer,
  IN typeid integer)
  IS 'Loads a single type member from the database.';


--
-- load_type_members(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION load_type_members(
  IN moduleid integer)
  RETURNS TABLE (
    id integer,
    name text,
	base_type integer,
	parent_id integer,
	"offset" integer,
	argument integer,
	number_of_elements integer)
  LANGUAGE SQL AS
$$

SELECT id, name, base_type, parent_id, "offset", argument, number_of_elements
  FROM bn_types
 WHERE module_id = $1;

$$;
COMMENT ON FUNCTION load_type_members(
  IN moduleid integer)
  IS 'Loads all type members for the given module.';

--
-- load_type_substitutions(IN moduleid integer)
--

CREATE OR REPLACE FUNCTION load_type_substitutions(
  IN moduleid integer)
  RETURNS TABLE (
    address bigint,
	"position" integer,
    expression_id integer,
	base_type_id integer,
	path integer[],
	"offset" integer)
  LANGUAGE SQL AS

$$

SELECT address, "position", expression_id, base_type_id, path, "offset"
  FROM bn_expression_types
 WHERE module_id = $1;

$$;
COMMENT ON FUNCTION load_type_substitutions(
  IN moduleid integer)
  IS 'Loads all type susbtitutions for a single module from the database.';

--
-- load_type_substitution(IN moduleid integer, IN address bigint, IN "position" integer, IN expression_id integer)
--

CREATE OR REPLACE FUNCTION load_type_substitution(
  IN moduleid integer,
  IN address bigint,
  IN "position" integer,
  IN expression_id integer)
  RETURNS TABLE (
    address bigint,
	"position" integer,
    expression_id integer,
	base_type_id integer,
	path integer[],
	"offset" integer)
  LANGUAGE SQL AS

$$

SELECT address, "position", expression_id, base_type_id, path, "offset"
  FROM bn_expression_types
 WHERE module_id = $1
   AND address = $2
   AND "position" = $3
   AND expression_id = $4;

$$;
COMMENT ON FUNCTION load_type_substitution(
  IN moduleid integer,
  IN address bigint,
  IN "position" integer,
  IN expression_id integer)
  IS 'Loads a aingle type susbtitutions.';

--
-- delete_type
--

CREATE OR REPLACE FUNCTION delete_type(
  IN module_id integer,
  IN type_id integer)
  RETURNS void
  LANGUAGE SQL AS
$$
DELETE FROM bn_types AS t
  WHERE t.module_id = $1
    AND t.id = $2;
$$;
COMMENT ON FUNCTION delete_type(IN moduleid integer, IN typeid integer)
  IS 'This function deletes a single type from bn_types also known as a member type if leaves a hole in the compound type at the member offset as large as the members size.';

--
-- update_member_offsets(IN module_id integer, IN moved_members integer[], IN delta integer, IN implicitly_moved_members integer[], IN implicit_delta integer)
--

CREATE OR REPLACE FUNCTION update_member_offsets(IN module_id integer, IN updated_members integer[], IN delta integer, IN implicitly_updated_members integer[], IN implicit_delta integer)
  RETURNS void LANGUAGE sql AS
$$
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
$$;
COMMENT ON FUNCTION update_member_offsets(IN module_id integer, IN updated_members integer[], IN delta integer, IN implicitly_updated_members integer[], IN implicit_delta integer)
  IS 'This function adjusts the offsets of the updated_members by delta and the offsets of implicitly_updated_members by implicit_dela, respectively.';

--
-- locate_type
--

CREATE OR REPLACE FUNCTION locate_type(IN moduleid integer, IN parentid integer, IN currentoffset integer)
  RETURNS integer LANGUAGE plpgsql AS
$$
SELECT bt.id FROM bn_types AS bt
  JOIN bn_base_types AS bbt
    ON bt.base_type = bbt.id
   AND bt.module_id = bbt.module_id
 WHERE bt.module_id = moduleid
   AND bt.parent_id = parentid
   AND bt.offset <= currentoffset
   AND bt.offset + bbt.size >= currentoffset
$$;
COMMENT ON FUNCTION locate_type(IN moduleid integer, IN parentid integer, IN currentoffset integer)
  IS 'Locates the bn_type id of a type at a given offset.';

--
-- move_type
--

CREATE OR REPLACE FUNCTION move_type(IN moduleid integer, IN old_parent_id integer, IN new_parent_id integer, IN type_id integer, IN newoffset integer)
  RETURNS void LANGUAGE plpgsql AS
$$
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

--
-- delete_type_member_compact_struct
--
CREATE OR REPLACE FUNCTION delete_type_compact(IN module_id integer, IN type_id integer)
  --RETURNS bn_types LANGUAGE plpgsql AS
  RETURNS integer LANGUAGE plpgsql AS
$$
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
COMMENT ON FUNCTION delete_type_compact(IN module_id integer, IN type_id integer)
  IS 'This function deletes a single type from bn_types and compacts the other elements in the compound type to leave no hole where the element has been.';
