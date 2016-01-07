/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.Database.NodeParser;

import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides instruction data from a database.
 */
public final class SqlCodeNodeProvider implements ICodeNodeProvider {
  /**
   * The result set of the query that requested the instruction data.
   */
  private final ResultSet m_resultSet;

  /**
   * Creates a new SQL provider object.
   * 
   * @param resultSet The result set of the query that requested the instruction data.
   */
  public SqlCodeNodeProvider(final ResultSet resultSet) {
    m_resultSet = resultSet;
  }

  @Override
  public IAddress getInstructionAddress() throws ParserException {
    try {
      return PostgreSQLHelpers.loadAddress(m_resultSet, "instruction_address");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public String getInstructionArchitecture() throws ParserException {
    try {
      return PostgreSQLHelpers.readString(m_resultSet, "architecture");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getBorderColor() throws ParserException {
    try {
      return m_resultSet.getInt("bordercolor");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getColor() throws ParserException {
    try {
      return m_resultSet.getInt("color");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public byte[] getData() throws ParserException {
    try {
      return m_resultSet.getBytes("instruction_data");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getExpressionTreeId() throws ParserException {
    try {
      return m_resultSet.getInt("expression_tree_id");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getExpressionTreeType() throws ParserException {
    try {
      return m_resultSet.getInt("expression_tree_type");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public IAddress getFunctionAddress() throws ParserException {
    try {
      return PostgreSQLHelpers.loadAddress(m_resultSet, "function_address");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public Integer getGlobalInstructionCommentId() throws ParserException {
    try {
      final int instructionCommentId = m_resultSet.getInt("global_instruction_comment");
      if (m_resultSet.wasNull()) {
        return null;
      }
      return instructionCommentId;
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public Integer getLocalInstructionCommentId() throws ParserException {
    try {
      final int localInstructionCommentId = m_resultSet.getInt("local_instruction_comment");
      if (m_resultSet.wasNull()) {
        return null;
      }
      return localInstructionCommentId;
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public Integer getGlobalNodeCommentId() throws ParserException {
    try {
      final int globalCodeNodeCommentId = m_resultSet.getInt("global_code_node_comment");
      if (m_resultSet.wasNull()) {
        return null;
      }
      return globalCodeNodeCommentId;
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public Integer getLocalNodeCommentId() throws ParserException {
    try {
      final int localCommentId = m_resultSet.getInt("local_code_node_comment");
      if (m_resultSet.wasNull()) {
        return null;
      }

      return localCommentId;
    } catch (final SQLException exception) {
      throw new ParserException(exception);
    }
  }

  @Override
  public double getHeight() throws ParserException {
    try {
      return m_resultSet.getDouble("height");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public String getImmediate() throws ParserException {
    try {
      return PostgreSQLHelpers.readString(m_resultSet, "immediate");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public String getMnemonic() throws ParserException {
    try {
      return PostgreSQLHelpers.readString(m_resultSet, "mnemonic");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getModule() throws ParserException {
    try {
      return m_resultSet.getInt("module_id");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getNodeId() throws ParserException {
    try {
      return m_resultSet.getInt("node_id");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public Integer getOperandPosition() throws ParserException {
    try {
      final int position = m_resultSet.getInt("operand_position");
      return m_resultSet.wasNull() ? null : position;
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public IAddress getParentFunction() throws ParserException {
    try {
      return PostgreSQLHelpers.loadAddress(m_resultSet, "parent_function");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getParentId() throws ParserException {
    try {
      return m_resultSet.getInt("expression_tree_parent_id");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public CReference getReference() throws ParserException {
    try {
      final IAddress address = PostgreSQLHelpers.loadAddress(m_resultSet, "target");
      if (address != null) {
        return new CReference(PostgreSQLHelpers.loadAddress(m_resultSet, "target"),
            ReferenceType.valueOf(m_resultSet.getString("address_references_type").toUpperCase()));
      } else {
        return null;
      }
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public String getReplacement() throws ParserException {
    try {
      return PostgreSQLHelpers.readString(m_resultSet, "replacement");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getSubstitutionOffset() throws ParserException {
    try {
      return m_resultSet.getInt("expression_types_offset");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public int getSubstitutionPosition() throws ParserException {
    try {
      return m_resultSet.getInt("expression_types_position");
    } catch (final SQLException exception) {
      throw new ParserException(exception);
    }
  }

  @Override
  public Integer getSubstitutionTypeId() throws ParserException {
    try {
      final Integer typeId = m_resultSet.getInt("expression_types_type");
      if (m_resultSet.wasNull()) {
        return null;
      } else {
        return typeId;
      }
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }


  @Override
  public String getSymbol() throws ParserException {
    try {
      return PostgreSQLHelpers.readString(m_resultSet, "symbol");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public Integer getTypeInstanceId() throws ParserException {
    try {
      final Integer typeInstanceId = m_resultSet.getInt("type_instance_id");
      if (m_resultSet.wasNull()) {
        return null;
      } else {
        return typeInstanceId;
      }
    } catch (final SQLException exception) {
      throw new ParserException(exception);
    }
  }

  @Override
  public double getWidth() throws ParserException {
    try {
      return m_resultSet.getDouble("width");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public double getX() throws ParserException {
    try {
      return m_resultSet.getDouble("x");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public double getY() throws ParserException {
    try {
      return m_resultSet.getDouble("y");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public boolean isAfterLast() throws ParserException {
    try {
      return m_resultSet.isAfterLast();
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public boolean isSelected() throws ParserException {
    try {
      return m_resultSet.getBoolean("selected");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public boolean isVisible() throws ParserException {
    try {
      return m_resultSet.getBoolean("visible");
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public boolean next() throws ParserException {
    try {
      return m_resultSet.next();
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public boolean prev() throws ParserException {
    try {
      return m_resultSet.previous();
    } catch (final SQLException e) {
      throw new ParserException(e);
    }
  }

  @Override
  public Integer[] getSubstitutionPath() throws ParserException {
    try {
      final Array array = m_resultSet.getArray("expression_types_path");
      if (m_resultSet.wasNull()) {
        return new Integer[0];
      } else {
        return (Integer[]) array.getArray();
      }
    } catch (final SQLException exception) {
      throw new ParserException(exception);
    }
  }
}
