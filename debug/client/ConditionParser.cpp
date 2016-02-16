// Copyright 2011-2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "ConditionParser.hpp"

#include "logger.hpp"
#include "includer.hpp"

#include <iostream>
#include <sstream>
#include <string>
#include <utility>

#include <zycon/src/bytereader.h>

const int ID_EXPRESSION_NODE = 0;
const int ID_FORMULA_NODE = 1;
const int ID_IDENTIFIER_NODE = 2;
const int ID_MEMORY_NODE = 3;
const int ID_NUMBER_NODE = 4;
const int ID_RELATION_NODE = 5;
const int ID_SUB_NODE = 6;

std::pair<ConditionNode*, std::vector<unsigned int> > invalidNode() {
  return std::make_pair((ConditionNode*)0, std::vector<unsigned int>());
}

bool parseChildren(zylib::zycon::ByteReader& reader,
                   std::vector<unsigned int>& children) {
  unsigned int childCount = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(LOG_ALWAYS,
                "Invalid node found: Child count read outside the buffer");

    return false;
  }

  msglog->log(LOG_VERBOSE, "Found %d children", childCount);

  for (unsigned int i = 0; i < childCount; i++) {
    unsigned int childId = ntohl(reader.readInt());

    children.push_back(childId);

    if (reader.isError()) {
      msglog->log(LOG_ALWAYS,
                  "Invalid node found: Child ID read outside the buffer");

      return false;
    }

    msglog->log(LOG_VERBOSE, "Found child ID %d", childId);
  }

  return true;
}

std::pair<ConditionNode*, std::vector<unsigned int> > parseExpressionNode(
    zylib::zycon::ByteReader& reader) {
  unsigned int operandSize = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid expression node found: Operand size read outside the buffer");

    return invalidNode();
  }

  if (operandSize != 2) {
    msglog->log(LOG_ALWAYS,
                "Invalid expression node found: Invalid operand size value");

    return invalidNode();
  }

  std::string operand = reader.readString(operandSize);

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid expression node found: Operator read outside the buffer");

    return invalidNode();
  }

  msglog->log(LOG_VERBOSE, "Found operand %s", operand.c_str());

  std::vector<unsigned int> children;

  return parseChildren(reader, children)
             ? std::make_pair(new ExpressionNode(operand), children)
             : invalidNode();
}

std::pair<ConditionNode*, std::vector<unsigned int> > parseFormulaNode(
    zylib::zycon::ByteReader& reader) {


  unsigned int operandSize = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid formula node found: Operand size read outside the buffer");

    return invalidNode();
  }

  if (operandSize != 1 && operandSize != 2) {
    msglog->log(LOG_ALWAYS,
                "Invalid formula node found: Invalid operand size value");

    return invalidNode();
  }

  std::string operand = reader.readString(operandSize);

  if (reader.isError()) {
    msglog->log(LOG_ALWAYS,
                "Invalid formula node found: Operator read outside the buffer");

    return invalidNode();
  }

  msglog->log(LOG_VERBOSE, "Found operand %s", operand.c_str());

  std::vector<unsigned int> children;

  return parseChildren(reader, children)
             ? std::make_pair(new FormulaNode(operand), children)
             : invalidNode();
}

std::pair<ConditionNode*, std::vector<unsigned int> > parseIdentifierNode(
    zylib::zycon::ByteReader& reader) {


  unsigned int operandSize = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid identifier node found: Operand size read outside the buffer");

    return invalidNode();
  }

  if (operandSize == 0) {
    msglog->log(LOG_ALWAYS,
                "Invalid identifier node found: Invalid operand size value");

    return invalidNode();
  }

  std::string identifier = reader.readString(operandSize);

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid identifier node found: Identifier read outside the buffer");

    return invalidNode();
  }

  msglog->log(LOG_VERBOSE, "Found identifier %s", identifier.c_str());

  std::vector<unsigned int> children;

  return parseChildren(reader, children)
             ? std::make_pair(new IdentifierNode(identifier), children)
             : invalidNode();
}

std::pair<ConditionNode*, std::vector<unsigned int> > parseMemoryNode(
    zylib::zycon::ByteReader& reader) {


  unsigned int operandSize = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid memory node found: Operand size read outside the buffer");

    return invalidNode();
  }

  if (operandSize != 0) {
    msglog->log(LOG_ALWAYS,
                "Invalid memory node found: Invalid operand size value");

    return invalidNode();
  }

  std::vector<unsigned int> children;

  return parseChildren(reader, children)
             ? std::make_pair(new MemoryNode(), children)
             : invalidNode();
}

std::pair<ConditionNode*, std::vector<unsigned int> > parseNumberNode(
    zylib::zycon::ByteReader& reader) {


  unsigned int operandSize = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid number node found: Operand size read outside the buffer");

    return invalidNode();
  }

  if (operandSize != 4) {
    msglog->log(LOG_ALWAYS,
                "Invalid number node found: Invalid operand size value");

    return invalidNode();
  }

  unsigned int value = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid number node found: Value size read outside the buffer");

    return invalidNode();
  }

  msglog->log(LOG_VERBOSE, "Found number node with value %d", value);

  std::vector<unsigned int> children;

  return parseChildren(reader, children)
             ? std::make_pair(new NumberNode(value), children)
             : invalidNode();
}

std::pair<ConditionNode*, std::vector<unsigned int> > parseRelationNode(
    zylib::zycon::ByteReader& reader) {


  unsigned int operandSize = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid relation node found: Operand size read outside the buffer");

    return invalidNode();
  }

  if (operandSize != 1 && operandSize != 2) {
    msglog->log(LOG_ALWAYS,
                "Invalid relation node found: Invalid operand size value");

    return invalidNode();
  }

  std::string operand = reader.readString(operandSize);

  if (reader.isError()) {
    msglog->log(
        LOG_ALWAYS,
        "Invalid number node found: Operator size read outside the buffer");

    return invalidNode();
  }

  std::vector<unsigned int> children;

  return parseChildren(reader, children)
             ? std::make_pair(new RelationNode(operand), children)
             : invalidNode();
}

std::pair<ConditionNode*, std::vector<unsigned int> > parseSubNode(
    zylib::zycon::ByteReader& reader) {


  unsigned int operandSize = ntohl(reader.readInt());

  if (reader.isError()) {
    msglog->log(LOG_ALWAYS,
                "Invalid sub node found: Operand size read outside the buffer");

    return invalidNode();
  }

  if (operandSize != 0) {
    msglog->log(LOG_ALWAYS,
                "Invalid sub node found: Invalid operand size value");

    return invalidNode();
  }

  std::vector<unsigned int> children;

  return parseChildren(reader, children)
             ? std::make_pair(new SubNode(), children)
             : invalidNode();
}

std::pair<ConditionNode*, std::vector<unsigned int> > parseNode(
    unsigned int type, zylib::zycon::ByteReader& reader) {


  msglog->log(LOG_VERBOSE, "Found node with type %d", type);

  switch (type) {
    case ID_EXPRESSION_NODE:
      return parseExpressionNode(reader);
    case ID_FORMULA_NODE:
      return parseFormulaNode(reader);
    case ID_IDENTIFIER_NODE:
      return parseIdentifierNode(reader);
    case ID_MEMORY_NODE:
      return parseMemoryNode(reader);
    case ID_NUMBER_NODE:
      return parseNumberNode(reader);
    case ID_RELATION_NODE:
      return parseRelationNode(reader);
    case ID_SUB_NODE:
      return parseSubNode(reader);
    default:
      msglog->log(LOG_ALWAYS, "Found node with invalid type %d", type);

      return invalidNode();
  }
}

NaviError parseConditionNodes(const char* p, const char* end,
                              ConditionNode*& node) {


  zylib::zycon::ByteReader reader(p, end - p);

  unsigned int counter = 0;

  std::map<unsigned int, ConditionNode*> nodes;
  std::map<ConditionNode*, std::vector<unsigned int> > nodeChildren;

  while (!reader.isDone()) {
    unsigned int type = ntohl(reader.readInt());

    if (reader.isError()) {
      msglog->log(LOG_ALWAYS,
                  "Invalid condition tree found: Type read outside the buffer");

      return NaviErrors::INVALID_CONDITION_TREE;
    }

    std::pair<ConditionNode*, std::vector<unsigned int> > node =
        parseNode(type, reader);

    if (node.first) {
      nodes[counter++] = node.first;
      nodeChildren[node.first] = node.second;
    } else {
      return NaviErrors::INVALID_CONDITION_TREE;
    }
  }

  msglog->log(LOG_VERBOSE,
              "Condition tree parsing is complete; reconstructing tree now");

  unsigned int ctr = 0;

  for (std::map<ConditionNode*, std::vector<unsigned int> >::iterator Iter =
           nodeChildren.begin();
       Iter != nodeChildren.end(); ++Iter) {
    ConditionNode* node = Iter->first;
    std::vector<unsigned int> children = Iter->second;

    for (std::vector<unsigned int>::iterator Iter2 = children.begin();
         Iter2 != children.end(); ++Iter2) {
      unsigned int childId = *Iter2;

      msglog->log(LOG_VERBOSE, "Linking nodes: %d -> %d", ctr, childId);

      if (nodes.find(childId) == nodes.end()) {
        return NaviErrors::INVALID_CONDITION_TREE;
      }

      ConditionNode* childNode = nodes[childId];

      node->addChild(childNode);
    }

    ++ctr;
  }

  node = nodes[0];

  return NaviErrors::SUCCESS;
}
